alter table application_resource_application_category
    add column applikasjonskategori_id bigint;

update application_resource_application_category arac
set applikasjonskategori_id = ak.id
from applikasjonskategori_kodeverk ak
where arac.application_category = ak.name
   or arac.application_category = ak.category;

delete from application_resource_application_category
where applikasjonskategori_id is null;

alter table application_resource_application_category
    drop column application_category;

alter table application_resource_application_category
    alter column applikasjonskategori_id set not null;

alter table application_resource_application_category
    drop constraint if exists FK8w4pgof2ej7ysm3ph3v2pqa1t;

alter table application_resource_application_category
    add constraint fk_app_res_app_category_resource
        foreign key (id)
            references application_resource (id)
            on delete cascade;

alter table application_resource_application_category
    add constraint fk_app_res_app_category_applikasjonskategori
        foreign key (applikasjonskategori_id)
            references applikasjonskategori_kodeverk (id)
            on delete cascade;

alter table applikasjonskategori_kodeverk
    drop column category;

alter table applikasjonskategori_kodeverk
    add constraint uk_applikasjonskategori_name unique (name);
