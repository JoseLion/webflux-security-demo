package db.migration;

import db.Migration;

public class V2CreateSessionTable extends Migration {

  @Override
  public String up() {
    return """
    BEGIN;
      CREATE EXTENSION IF NOT EXISTS hstore;

      CREATE TABLE session (
        id UUID NOT NULL PRIMARY KEY,
        original_id UUID NOT NULL,
        session_attrs HSTORE NOT NULL,
        creation_time TIMESTAMP WITH TIME ZONE NOT NULL,
        last_accessed_time TIMESTAMP WITH TIME ZONE NOT NULL,
        max_inactive_interval BIGINT NOT NULL,
        version BIGINT NOT NULL
      );
    COMMIT;
    """;
  }

  @Override
  public String down() {
    return "DROP TABLE session;";
  }
}
