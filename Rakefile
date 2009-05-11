def mvn(*args)
  ensure_cmd("mvn")
  args.unshift("-o") if ENV["OFFLINE"] == "1"
  system("mvn", *args)
  return $?.exitstatus == 0
end

def mvn_or_die(*args)
  if !mvn(*args)
    yield if block_given?
    exit(-1)
  end
end

def cmd?(name)
  %x{which #{name}}
  return $?.exitstatus == 0
end

def ensure_cmd(name)
  if not cmd?(name)
    $stderr.puts "Unable to find command '#{name}'! Make sure it is installed and in your PATH (#{ENV['PATH']})"
    exit(-1)
  end
end

desc "Runs bouncer."
task :run do
  mvn_or_die("exec:java")
end

task :default => [:test]

desc "Runs the tests."
task :test do
  mvn_or_die("clean", "test") do
    for test_file in Dir["target/surefire-reports/*.txt"]
      test_results = File.read(test_file)
      if test_results =~ /FAILURE/m
        puts test_results
      end
    end
  end
end

namespace :test do
  desc "Generates an HTML coverage report and opens it in your default browser. NO_OPEN=1 to just generate the report."
  task :coverage do
    mvn_or_die("clean", "cobertura:cobertura")
    system("open", "target/site/cobertura/index.html") unless ENV["NO_OPEN"] == "1"
  end

  desc "Generates an HTML test report and opens it in your default browser. NO_OPEN=1 to just generate the report."
  task :report do
    mvn_or_die("clean", "surefire-report:report")
    system("open", "target/site/surefire-report.html") unless ENV["NO_OPEN"] == "1"
  end
end

desc "Build a deliverable JAR file."
task :jar do
  require "time"
  head = `git log -n1 --pretty="format:%H"`
  @version = "#{Time.now.utc.strftime("%Y%m%d%H%M%S")}-#{head[0..7]}"
  mvn_or_die("maven.test.skip=true", "clean", "assembly:assembly")
  generated_jar_file = Dir["target/*-with-dependencies.jar"].first
  @jar_file = "target/" + File.basename(generated_jar_file).gsub(/[\d]+.[\d]+(-SNAPSHOT)?-jar-with-dependencies/, @version)
  mv(generated_jar_file, @jar_file)
end

def deployer
  require "tasks/deploy"
  if ENV["STAGING"] == "1"
    Deploy.send(:hosts, %w{ bouncer1.stage })
  else
    Deploy.send(:hosts, %w{ bouncer1.prod })
  end
  return Deploy.new
end

desc "Push the latest version to the servers."
task :deploy => [:jar] do
  deployer.run(@jar_file)
end

namespace :deploy do
  desc "Prints all installed versions."
  task :installed do
    deployer.print_installed_versions
  end

  desc "Cleans all but the 5 most recent installed versions."
  task :clean do
    deployer.clean_installed
  end
end
