alter table application_resource_location ADD resource_ref bigint;
create index resource_ref_index on application_resource_location (resource_ref);
