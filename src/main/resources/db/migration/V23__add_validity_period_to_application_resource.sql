alter table application_resource ADD COLUMN valid_from timestamp;
alter table application_resource ADD COLUMN valid_to timestamp;
alter table application_resource DROP COLUMN date_created;
