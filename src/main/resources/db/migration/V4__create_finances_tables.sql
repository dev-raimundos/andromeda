CREATE SCHEMA IF NOT EXISTS finances;

CREATE TABLE finances.scope
(
    id         UUID                        NOT NULL,
    name       VARCHAR(255)                NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_scope PRIMARY KEY (id)
);

CREATE TABLE finances.category
(
    id         UUID                        NOT NULL,
    scope_id   UUID                        NOT NULL,
    name       VARCHAR(255)                NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (id)
);

ALTER TABLE finances.category
    ADD CONSTRAINT FK_CATEGORY_ON_SCOPE FOREIGN KEY (scope_id) REFERENCES finances.scope (id);

CREATE TABLE finances.subcategory
(
    id          UUID                        NOT NULL,
    category_id UUID                        NOT NULL,
    name        VARCHAR(255),               NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_subcategory PRIMARY KEY (id)
);

ALTER TABLE finances.subcategory
    ADD CONSTRAINT FK_SUBCATEGORY_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES finances.category (id);

CREATE TABLE finances.list
(
    id              UUID                           NOT NULL,
    reference_month TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    created_at      TIMESTAMP(6) WITH TIME ZONE    NOT NULL,
    updated_at      TIMESTAMP(6) WITH TIME ZONE    NOT NULL,
    CONSTRAINT pk_list PRIMARY KEY (id)
);

CREATE TABLE finances.entry
(
    id             UUID                        NOT NULL,
    subcategory_id UUID                        NOT NULL,
    list_id        UUID                        NOT NULL,
    paid_at        TIMESTAMP(6) WITHOUT TIME ZONE,
    description    VARCHAR(255),
    due_date       TIMESTAMP(6) WITHOUT TIME ZONE,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_entry PRIMARY KEY (id)
);

ALTER TABLE finances.entry
    ADD CONSTRAINT FK_ENTRY_ON_LIST FOREIGN KEY (list_id) REFERENCES finances.list (id);

ALTER TABLE finances.entry
    ADD CONSTRAINT FK_ENTRY_ON_SUBCATEGORY FOREIGN KEY (subcategory_id) REFERENCES finances.subcategory (id);