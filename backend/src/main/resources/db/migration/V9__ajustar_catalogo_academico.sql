-- Libros de referencia para Ciencias de la Computación.
UPDATE catalogo_items
SET titulo = 'The C Programming Language',
    isbn = '9780131103627',
    autor = 'Brian W. Kernighan y Dennis M. Ritchie',
    editorial = 'Prentice Hall',
    anio_publicacion = 1988,
    descripcion = 'Referencia clásica para fundamentos del lenguaje C y programación de sistemas.'
WHERE id = '00000000-0000-0000-0000-000000000125';

UPDATE catalogo_items
SET titulo = 'The C++ Programming Language',
    isbn = '9780321563842',
    autor = 'Bjarne Stroustrup',
    editorial = 'Addison-Wesley',
    anio_publicacion = 2013,
    descripcion = 'Referencia del lenguaje C++ y sus herramientas para desarrollo de software.'
WHERE id = '00000000-0000-0000-0000-000000000126';

UPDATE catalogo_items
SET titulo = 'Pelota de vóley', descripcion = 'Pelota oficial para práctica y entrenamiento de voleibol.'
WHERE id = '00000000-0000-0000-0000-000000000128';
UPDATE catalogo_items
SET titulo = 'Pelota de fútbol', descripcion = 'Balón de fútbol para entrenamiento y actividades deportivas.'
WHERE id = '00000000-0000-0000-0000-000000000129';
UPDATE catalogo_items
SET titulo = 'Pelota de básquet', descripcion = 'Balón de baloncesto para entrenamiento y actividades deportivas.'
WHERE id = '00000000-0000-0000-0000-000000000130';
