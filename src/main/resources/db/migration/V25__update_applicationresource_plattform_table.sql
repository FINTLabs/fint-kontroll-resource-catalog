alter table application_resource_platform
    add column plattform_id bigint;

update application_resource_platform arp
set plattform_id = pk.id
from plattform_kodeverk pk
where arp.platform = pk.name;

delete from application_resource_platform
where plattform_id is null;

alter table application_resource_platform
    drop column platform;

alter table application_resource_platform
    alter column plattform_id set not null;

alter table application_resource_platform
    drop constraint if exists fk65hwdpt5pfioanqbjw94g88qx;

alter table application_resource_platform
    add constraint fk_app_res_platform_resource
        foreign key (id)
            references application_resource (id)
            on delete cascade;

alter table application_resource_platform
    add constraint fk_app_res_app_platform_plattform
        foreign key (plattform_id)
            references plattform_kodeverk (id)
            on delete cascade;
