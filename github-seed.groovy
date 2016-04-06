/**
 * https://jenkinsci.github.io/job-dsl-plugin/#
 * Parameters :
 * @param projectName (String). Github's project name, will be used as base for every job name
 * @param owner (String). Github's project owner
 * @param branch (String). The branch to build
 */

String githubURI = "$owner/$projectName"
String githubURL = "https://github.com/$githubURI"
String buildJob = "${projectName}-build"
String releaseJob = "${projectName}-release"
String testReportName = "Tests for $projectName"
String coverageReportName = "Code coverage for $projectName"
String pipelineName = projectName

job(buildJob) {
  properties {
    githubProjectUrl githubURL
  }
  scm {
    github githubURI, branch, 'ssh'
  }
  triggers {
    githubPush()
  }
  steps {
    gradle {
      useWrapper true
      tasks 'clean'
      tasks 'test'
    }
  }
  publishers {
    publishHtml {
      report('build/reports/tests') {
        reportName testReportName
        alwaysLinkToLastBuild()
      }
      report('build/reports/jacoco/test') {
        reportName coverageReportName
        alwaysLinkToLastBuild()
      }
      publishCloneWorkspace '**/*', '', 'Successful'
      buildPipelineTrigger releaseJob
    }
  }
}

job(releaseJob) {
  scm {
    cloneWorkspace buildJob, 'Successful'
  }
  steps {
    gradle {
      useWrapper true
      tasks 'tasks'
    }    
  }
}

buildPipelineView(pipelineName) {
  selectedJob buildJob
  displayedBuilds 20
  triggerOnlyLatestJob true
}

