-- One-off historical cleanup for legacy performance schema reset.
-- Apply manually once through the migration process if the old destructive reset is still needed.

DROP TABLE IF EXISTS peer_review;
DROP TABLE IF EXISTS team_evaluation;
DROP TABLE IF EXISTS performance_attachment;
DROP TABLE IF EXISTS monthly_performance;
DROP TABLE IF EXISTS evaluation;
DROP TABLE IF EXISTS performance_personal;
DROP TABLE IF EXISTS performance_team;
DROP TABLE IF EXISTS performance;
