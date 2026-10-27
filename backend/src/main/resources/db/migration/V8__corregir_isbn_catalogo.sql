-- Corrige el ISBN de un libro inicial para cumplir la validación ISBN-13 del dominio.
UPDATE catalogo_items
SET isbn = '9780307476463'
WHERE id = '00000000-0000-0000-0000-000000000126';
