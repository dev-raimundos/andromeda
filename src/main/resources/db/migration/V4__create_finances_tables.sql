CREATE SCHEMA IF NOT EXISTS finances;

-- SCOPE
CREATE TABLE finances.scope
(
    id         UUID         NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_scope PRIMARY KEY (id)
);

-- CATEGORY
CREATE TABLE finances.category
(
    id         UUID         NOT NULL,
    scope_id   UUID         NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_category PRIMARY KEY (id),
    CONSTRAINT fk_category_scope
        FOREIGN KEY (scope_id)
        REFERENCES finances.scope (id)
);

-- SUBCATEGORY
CREATE TABLE finances.subcategory
(
    id          UUID         NOT NULL,
    category_id UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_subcategory PRIMARY KEY (id),
    CONSTRAINT fk_subcategory_category
        FOREIGN KEY (category_id)
        REFERENCES finances.category (id)
);

-- LIST
CREATE TABLE finances.list
(
    id              UUID        NOT NULL,
    reference_month DATE        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_list PRIMARY KEY (id)
);

-- ENTRY
CREATE TABLE finances.entry
(
    id             UUID         NOT NULL,
    subcategory_id UUID         NOT NULL,
    list_id        UUID         NOT NULL,
    paid_at        DATE,
    description    VARCHAR(255),
    due_date       DATE,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_entry PRIMARY KEY (id),

    CONSTRAINT fk_entry_list
        FOREIGN KEY (list_id)
        REFERENCES finances.list (id),

    CONSTRAINT fk_entry_subcategory
        FOREIGN KEY (subcategory_id)
        REFERENCES finances.subcategory (id)
);