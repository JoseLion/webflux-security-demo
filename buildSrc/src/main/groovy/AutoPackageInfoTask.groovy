import groovy.io.FileType
import groovy.text.SimpleTemplateEngine

import javax.inject.Inject

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

public class AutoPackageInfoTask extends DefaultTask {

  private String generatedDir

  @Inject
  public AutoPackageInfoTask(String generatedDir) {
    super();
    this.generatedDir = generatedDir
  }

  @TaskAction
  def autoPackageInfo() {
    def packages = [] as Set

    new File(".").eachFileRecurse(FileType.FILES) {
      if (it.name.endsWith('.java')) {
        packages << ((it.text =~ 'package (.+);')[0][1])
      }
    }

    packages.each { createPackageInfo(it) }
  }

  def void createPackageInfo(package) {
    def dotToSlash = package.replaceAll('\\.', '/')
    def dir = this.project.mkdir("${this.generatedDir}/${dotToSlash}")
    File outputFile = new File(dir.absolutePath, 'package-info.java')
    String templateOutput = applyPackageInfoTemplate(package)

    outputFile.bytes = []
    outputFile << templateOutput
  }

  def applyPackageInfoTemplate(packageName) {
    def engine = new SimpleTemplateEngine()
    def template = """@NonNullApi
@NonNullFields
package $packageName;

import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;
"""
    def params = ["packageName": packageName]
    engine.createTemplate(template).make(params).toString()
  }
}
