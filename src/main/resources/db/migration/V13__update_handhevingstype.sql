UPDATE handhevingstype_kodeverk
SET label = 'FREEALL'
WHERE label = 'FREE_ALL';

UPDATE handhevingstype_kodeverk
SET label = 'FREESTUDENT'
WHERE label = 'FREE_STUDENT';

UPDATE handhevingstype_kodeverk
SET label = 'FREEEDU'
WHERE label = 'FREE_EDU';

INSERT INTO handhevingstype_kodeverk (fk_label, label) VALUES ('Ikke satt håndhevingstype', 'NOTSET');
