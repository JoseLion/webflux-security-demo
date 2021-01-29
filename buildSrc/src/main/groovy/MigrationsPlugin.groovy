import org.gradle.api.Project
import org.gradle.api.Plugin

public class MigrationsPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.tasks.create('migrate', MigrationTask, '')

    project.tasks.create('migrateTest', MigrationTask, 'test')

    project.tasks.create('undoMigration', UndoMigrationTask, '')

    project.tasks.create('undoMigrationTest', UndoMigrationTask, 'test')
  }
}
