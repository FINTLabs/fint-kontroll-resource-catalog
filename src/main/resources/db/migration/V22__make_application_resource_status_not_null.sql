update application_resource
set status = 'INACTIVE'
where status is null;

alter table application_resource
    alter column status set not null;
