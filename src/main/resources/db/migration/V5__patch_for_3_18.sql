alter table application_resource ADD COLUMN license_enforcement varchar(255);
alter table application_resource ADD COLUMN has_cost boolean NOT NULL DEFAULT false;
alter table application_resource ADD COLUMN unit_cost bigint;
alter table application_resource ADD COLUMN status varchar(255);
alter table application_resource ADD COLUMN status_changed timestamp;