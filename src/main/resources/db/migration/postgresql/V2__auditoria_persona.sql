ALTER TABLE persona
    ADD COLUMN creado_en      TIMESTAMP,
    ADD COLUMN creado_por     VARCHAR(60),
    ADD COLUMN modificado_en  TIMESTAMP,
    ADD COLUMN modificado_por VARCHAR(60);
