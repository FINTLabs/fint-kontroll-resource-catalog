alter table application_resource ADD created_date timestamp with time zone;
alter table application_resource ADD modified_date timestamp with time zone;
alter table application_resource ADD modified_by varchar(255);
