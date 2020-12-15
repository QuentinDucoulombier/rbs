ALTER TABLE rbs.unavailability
DROP CONSTRAINT resource_fk;

ALTER TABLE rbs.unavailability
ADD CONSTRAINT resource_fk FOREIGN KEY (resource_id) REFERENCES rbs.resource(id) ON UPDATE NO ACTION ON DELETE CASCADE;
