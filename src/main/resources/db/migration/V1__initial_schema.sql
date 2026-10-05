CREATE SCHEMA IF NOT EXISTS tenant_a;
CREATE SCHEMA IF NOT EXISTS tenant_b;

-- SHARED DATA

CREATE TABLE public.app_users(
    id BIGINT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    tenant_schema VARCHAR(50) NOT NULL
);

-- TENANT A

CREATE TABLE tenant_a.customers(
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(150) NOT NULL
);

-- TENANT B

CREATE TABLE tenant_b.customers(
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(150) NOT NULL
);

-- DEMO USERS

INSERT INTO public.app_users(id, email, tenant_schema)
    VALUES(1, 'johndoe@demo.local', 'tenant_a'),
          (2, 'janedoe@demo.local', 'tenant_b');

-- TENANT A DATA

INSERT INTO tenant_a.customers(name) VALUES('Acme Customer A'),('Acme Customer B');

--TENANT B DATA

INSERT INTO tenant_b.customers(name) VALUES('Globex Customer A'),('Globex Customer B');