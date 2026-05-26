---
tags: [feature, friend-challenge, check-in, ranking]
date: 2026-05-25
status: done
---

# Feature: Check-ins em desafios entre amigos com ranking por tipo

## Objetivo

Permitir que participantes de um [[friend-challenges|desafio entre amigos]] registrem atividades (check-ins) e visualizem um ranking calculado em tempo real conforme o tipo de desafio.

---

## Regras de negócio

- **Pace calculado automaticamente** no registro: `round(durationSeconds / distanceKm)` em seg/km.
- **Dia de auditoria bloqueado**: não é permitido registrar check-in no `endDate` do desafio → `403 CheckInAuditDateException`.
- **Duplicata por dia (CHECK_IN)**: apenas um check-in por usuário por dia em desafios do tipo `CHECK_IN` → `409 DuplicateCheckInException`.
- **Status padrão**: todo check-in nasce com `status = VALID`. Auditoria manual está fora do escopo atual.

### Lógica de score por tipo

| Tipo | Métrica | Melhor |
|---|---|---|
| `DISTANCE` | Soma de `distanceKm` | Maior |
| `ACTIVITY_TIME` | Soma de `durationSeconds` | Maior |
| `PACE` | Menor `paceSecondsPerKm` de check-ins com `distanceKm >= goalValue` | Menor |
| `CHECK_IN` | Dias distintos com check-in | Maior |

- **Empate**: posição compartilhada; próximo posicionamento pula (ex: 1, 1, 3).
- Para `PACE`, check-ins abaixo da distância mínima (`goalValue`) são ignorados. Se nenhum check-in for elegível, o score é `Double.MAX_VALUE`.

---

## Backend

- **Endpoints**: `CheckInController` em `/api/v1/friend-challenges/{challengeId}/`
- **Serviço**: `CheckInService` — registro, listagem com `userName`, ranking por tipo.
- **Persistência**: tabela `friend_challenge_check_ins` (migration `V8`).

### Tabela `friend_challenge_check_ins`

Colunas principais: `id`, `friend_challenge_id`, `user_id`, `distance_km`, `duration_seconds`, `pace_seconds_per_km`, `check_in_date`, `notes`, `status`, `created_at`.

Índices: `(friend_challenge_id)`, `(user_id)`, `(friend_challenge_id, user_id, check_in_date)`.

---

## Mobile

- **Tela de detalhe** (`[id].tsx`): seção de ranking com top 3 destacados + botão "Registrar check-in".
- **Tela de registro** (`RegisterCheckInScreen`): formulário com distância, duração, data, observação e preview de pace calculado.
- **Rota**: `app/(app)/challenges/friend/[id]/register-check-in.tsx`
- **Types**: `CheckInResponse`, `RegisterCheckInRequest`, `RankingEntry`, `RankingResponse` em `challenge.types.ts`.
- **Service**: `registerCheckIn`, `getCheckIns`, `getChallengeRanking` em `challengeService.ts`.

---

## Contratos

### POST /api/v1/friend-challenges/{id}/check-ins

Autenticação: Sim (JWT)

Body:
```json
{
  "distanceKm": 10.5,
  "durationSeconds": 3600,
  "checkInDate": "2026-05-24",
  "notes": "Corrida matinal"
}
```

Response `201`:
```json
{
  "id": "uuid",
  "challengeId": "uuid",
  "userId": "uuid",
  "distanceKm": 10.5,
  "durationSeconds": 3600,
  "paceSecondsPerKm": 343,
  "checkInDate": "2026-05-24",
  "notes": "Corrida matinal",
  "status": "VALID",
  "createdAt": "2026-05-24T10:00:00Z"
}
```

Erros: `403` (auditoria ou sem participação), `409` (duplicata no mesmo dia para CHECK_IN).

---

### GET /api/v1/friend-challenges/{id}/check-ins

Autenticação: Sim (JWT)

Response `200`: array de check-ins com `userName` resolvido em tempo real via `UserRepository`.

---

### GET /api/v1/friend-challenges/{id}/ranking

Autenticação: Sim (JWT)

Response `200`:
```json
{
  "challengeType": "DISTANCE",
  "entries": [
    { "position": 1, "userId": "uuid", "name": "Ana", "score": 42.0, "checkInCount": 5 }
  ]
}
```

---

## Decisões técnicas

- **Ranking calculado on-demand** (`getRanking` lê todos os check-ins e computa na memória da JVM). Não há job de background nem cache — adequado para grupos pequenos. Revisar se o volume crescer.
- **`userName` resolvido por N queries** em `listCheckIns` e `buildRanking`. Candidato a otimização com JOIN quando escalar.
- **`paceSecondsPerKm`** é calculado e armazenado na gravação (`Math.round`), não recalculado em leitura.
- **`status`** existe na tabela para suportar auditoria futura sem quebrar schema.

---

## Arquivos principais

**Backend**
- `src/main/java/com/pacefriends/api/friendchallenge/application/CheckInService.java`
- `src/main/java/com/pacefriends/api/friendchallenge/presentation/CheckInController.java`
- `src/main/java/com/pacefriends/api/friendchallenge/domain/FriendChallengeCheckIn.java`
- `src/main/resources/db/migration/V8__create_friend_challenge_check_ins.sql`

**Mobile**
- `mobile/src/features/challenge/screens/RegisterCheckInScreen.tsx`
- `mobile/src/features/challenge/services/challengeService.ts`
- `mobile/app/(app)/challenges/friend/[id]/register-check-in.tsx`
- `mobile/app/(app)/challenges/friend/[id].tsx`
