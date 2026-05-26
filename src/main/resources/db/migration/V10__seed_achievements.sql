INSERT INTO achievements (slug, name, description, criteria_type, criteria_value, icon_key) VALUES
('primeiro-passo',      'Primeiro Passo',       'Registrou a primeira atividade em um desafio',          'FIRST_ACTIVITY',                null, 'footsteps'),
('criador-de-desafios', 'Criador de Desafios',  'Criou o primeiro desafio individual',                   'FIRST_CHALLENGE_CREATED',       null, 'flag'),
('competidor',          'Competidor',           'Participou do primeiro desafio entre amigos',            'FIRST_FRIEND_CHALLENGE_JOINED', null, 'groups'),
('primeira-vitoria',    'Primeira Vitoria',     'Venceu o primeiro desafio entre amigos',                'FIRST_VICTORY',                 null, 'trophy'),
('corredor-iniciante',  'Corredor Iniciante',   'Registrou 5 atividades em desafios',                    'ACTIVITIES_COUNT',              5,    'runner'),
('constancia',          'Constancia',           'Manteve sequencia de 7 dias consecutivos',              'STREAK_DAYS',                   7,    'fire'),
('foguinho-aceso',      'Foguinho Aceso',       'Manteve sequencia de 3 dias consecutivos',              'STREAK_DAYS',                   3,    'flame'),
('evoluiu',             'Evoluiu',              'Avancou de nivel pela primeira vez',                    'LEVEL_UP',                      null, 'arrow_upward'),
('trilha-completa',     'Trilha Completa',      'Completou uma trilha de treinamento',                   'TRAINING_PATH_COMPLETED',       null, 'map')
ON CONFLICT (slug) DO NOTHING;
