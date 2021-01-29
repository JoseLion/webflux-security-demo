package db;

import com.github.joselion.maybe.Maybe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.r2dbc.core.DatabaseClient;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootApplication
public class UndoMigration {

  public static void main(final String[] args) {
    final var context = SpringApplication.run(UndoMigration.class, args);
    final var client = context.getBeanFactory().getBean(DatabaseClient.class);

    final String script = client.sql("SELECT script FROM flyway_schema_history ORDER BY installed_on DESC LIMIT 1;")
      .map((row, metadata) -> row.get("script", String.class))
      .first()
      .block();

    log.info("🔍 Last migration script: " + script);

    final String downSql = Maybe.resolve(() -> {
      final var migration = context.getClassLoader().loadClass(script);
      return (String) migration.getMethod("down")
        .invoke(migration.getDeclaredConstructor().newInstance());
    })
    .orDefault("");

    if (downSql.isEmpty()) {
      log.error("💢 No down migration found!");
      exitApplication(context, 999);
    }

    log.info("💥 Undoing last migration...");

    final var deleteQuery = """
      BEGIN;
        %s;
        DELETE FROM flyway_schema_history WHERE script='%s';
      COMMIT;
    """
    .formatted(downSql, script);

    client.sql(deleteQuery)
      .fetch()
      .rowsUpdated()
      .doOnError(error -> {
        log.error("❌ Failed to undo migration: ");
        error.printStackTrace();

        exitApplication(context, 999);
      })
      .block();

    log.info("♻ Last migration undone!");
    exitApplication(context, 0);
  }

  private static void exitApplication(final ConfigurableApplicationContext context, final int exitCode) {
    final var code = SpringApplication.exit(context, () -> exitCode);
    System.exit(code);
  }
}
