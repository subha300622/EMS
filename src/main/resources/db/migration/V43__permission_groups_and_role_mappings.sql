CREATE TABLE IF NOT EXISTS permission_groups (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS permission_group_mappings (
    group_id BIGINT NOT NULL REFERENCES permission_groups(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (group_id, permission_id)
);

CREATE TABLE IF NOT EXISTS role_permission_groups (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    group_id BIGINT NOT NULL REFERENCES permission_groups(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, group_id)
);

CREATE TABLE IF NOT EXISTS role_direct_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);
