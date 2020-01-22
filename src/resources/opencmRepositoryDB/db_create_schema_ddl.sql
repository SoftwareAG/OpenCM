-- ***************************************************************************
-- 
--    Create Repository data tables for all OpenCM functions
-- 
-- ***************************************************************************
-- Organisation
CREATE TABLE ORGANISATION (
  ORGANISATION_NAME	VARCHAR(128)	NOT NULL
);
ALTER TABLE ORGANISATION
  ADD CONSTRAINT ORGANISATION_PK PRIMARY KEY (ORGANISATION_NAME);
  
-- Department
CREATE TABLE DEPARTMENT (
  DEPARTMENT_NAME	VARCHAR(128)	NOT NULL,
  ORGANISATION_NAME	VARCHAR(128)	NOT NULL
);
ALTER TABLE DEPARTMENT
  ADD CONSTRAINT DEPARTMENT_FK FOREIGN KEY (ORGANISATION_NAME)
      REFERENCES ORGANISATION(ORGANISATION_NAME) ON DELETE CASCADE;

-- Server
CREATE TABLE SERVER (
  SERVER_NAME		VARCHAR(128)	NOT NULL,
  DEPARTMENT_NAME	VARCHAR(128)	NOT NULL
);
ALTER TABLE SERVER
  ADD CONSTRAINT SERVER_FK FOREIGN KEY (DEPARTMENT_NAME)
      REFERENCES DEPARTMENT(DEPARTMENT_NAME) ON DELETE CASCADE;

-- Server Properties
CREATE TABLE SERVER_PROPS (
  SERVER_NAME		VARCHAR(128)	NOT NULL,
  DEPARTMENT_NAME	VARCHAR(128)	NOT NULL
  KEY				VARCHAR(128)	NOT NULL,
  VALUE				VARCHAR(128)	NOT NULL
);
ALTER TABLE SERVER_PROPS
  ADD CONSTRAINT SERVER_PROPS_FK1 FOREIGN KEY (SERVER_NAME,DEPARTMENT_NAME)
      REFERENCES SERVER(SERVER_NAME) ON DELETE CASCADE;
ALTER TABLE SERVER_PROPS
  ADD CONSTRAINT SERVER_PROPS_FK2 FOREIGN KEY (DEPARTMENT_NAME)
      REFERENCES DEPARTMENT(DEPARTMENT_NAME) ON DELETE CASCADE;

-- Installation
CREATE TABLE INSTALLATION (
  INSTALLATION_NAME	VARCHAR(128)	NOT NULL,
  SERVER_NAME		VARCHAR(128)	NOT NULL,
  REPO_TYPE			VARCHAR(32)	NOT NULL
);
ALTER TABLE INSTALLATION
  ADD CONSTRAINT INSTALLATION_FK FOREIGN KEY (SERVER_NAME)
      REFERENCES SERVER(SERVER_NAME) ON DELETE CASCADE;

-- Installation Properties
CREATE TABLE INSTALLATION_PROPS (
  SERVER_NAME		VARCHAR(128)	NOT NULL,
  INSTALLATION_NAME	VARCHAR(128)	NOT NULL,
  KEY		VARCHAR(128)	NOT NULL,
  VALUE		VARCHAR(128)	NOT NULL
);
ALTER TABLE INSTALLATION_PROPS
  ADD CONSTRAINT INSTALLATION_PROPS_FK FOREIGN KEY (INSTALLATION_NAME)
      REFERENCES INSTALLATION(INSTALLATION_NAME) ON DELETE CASCADE;
	  
-- Component
CREATE TABLE COMPONENT (
  COMPONENT_NAME	VARCHAR(256)	NOT NULL,
  INSTALLATION_NAME	VARCHAR(128)	NOT NULL
);
ALTER TABLE COMPONENT
  ADD CONSTRAINT COMPONENT_PK PRIMARY KEY (COMPONENT_NAME);

ALTER TABLE COMPONENT
  ADD CONSTRAINT COMPONENT_FK FOREIGN KEY (INSTALLATION_NAME)
      REFERENCES INSTALLATION(INSTALLATION_NAME) ON DELETE CASCADE;

-- Instance
CREATE TABLE INSTANCE (
  INSTANCE_NAME		VARCHAR(256)	NOT NULL,
  COMPONENT_NAME	VARCHAR(256)	NOT NULL
);
ALTER TABLE INSTANCE
  ADD CONSTRAINT INSTANCE_PK PRIMARY KEY (INSTANCE_NAME);

ALTER TABLE INSTANCE
  ADD CONSTRAINT INSTANCE_FK FOREIGN KEY (COMPONENT_NAME)
      REFERENCES COMPONENT(COMPONENT_NAME) ON DELETE CASCADE;

-- Properties
CREATE TABLE PROPERTY (
  INSTALLATION_NAME	VARCHAR(128)	NOT NULL,
  COMPONENT_NAME	VARCHAR(256) 	NOT NULL,
  INSTANCE_NAME		VARCHAR(256)	NOT NULL,
  KEY				VARCHAR(256)	NOT NULL,
  VALUE				VARCHAR(1024)	NOT NULL
);
ALTER TABLE PROPERTY
  ADD CONSTRAINT PROPERTY_FK FOREIGN KEY (INSTANCE_NAME)
      REFERENCES INSTANCE(INSTANCE_NAME) ON DELETE CASCADE;


