import groovy.io.FileType
import groovy.text.SimpleTemplateEngine

import org.gradle.api.Project
import org.gradle.api.Plugin

public class AutoPackageInfoPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    String generatedDir = "$project.buildDir/generated"

    project.sourceSets.main.java.srcDirs(generatedDir)

    project.tasks.create(
      'autoPackageInfo',
      AutoPackageInfoTask,
      generatedDir
    )

    project.tasks.classes.finalizedBy(project.tasks.autoPackageInfo)
  }
}
