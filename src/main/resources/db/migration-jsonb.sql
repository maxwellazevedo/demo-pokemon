-- Script de migração: converte colunas TEXT para jsonb
-- Execute manualmente no banco PostgreSQL local antes de subir a aplicação
-- se a tabela pokemon já existia com colunas TEXT.
--
-- Se a tabela não existia ainda, o Hibernate criará automaticamente com jsonb
-- (requer ddl-auto: create ou create-drop).

ALTER TABLE pokemon
    ALTER COLUMN abilities SET DATA TYPE jsonb USING abilities::jsonb,
    ALTER COLUMN moves SET DATA TYPE jsonb USING moves::jsonb;
