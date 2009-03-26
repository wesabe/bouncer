require "robot-army"
require "highline/import"

class Deploy < RobotArmy::TaskMaster
  def app
    return "bouncer"
  end
  
  def root
    return "/opt/#{app}"
  end
  
  def deploy_path(jar_location)
    return File.join(root, File.basename(jar_location))
  end
  
  def current_link
    return File.join(root, "current.jar")
  end
  
  def clean_installed
    installed = sudo do
      Dir[File.join(root, "bouncer-*.jar")] 
    end - [current_filename]
    cleanable = installed.sort[0..-6]
    if cleanable.any?
      say "Removing old installed versions: "
      for filename in cleanable
        puts "   * #{filename}"
      end
      sudo do
        for filename in cleanable
          FileUtils.rm(filename)
        end
      end
    else
      say "No cleanable versions installed."
    end
  end
  
  def print_installed_versions
    say "Installed versions:"
    current_version = convert_to_version(current_filename)
    versions = remote do
      %x{ ls #{root}/bouncer-*.jar }
    end.split("\n")
    versions.each do |filename|
      version = convert_to_version(filename)
      if current_version == version
        puts "   #{HighLine.new.color("*", :bold)} #{HighLine.new.color(version, :underline, :bold)}"
      else
        puts "     #{version}"
      end
    end
  end
  
  def convert_to_version(filename)
    filename =~ /bouncer-(.*)\.jar/
    return $1
  end
  
  def current_filename
    remote do
      File.readlink(current_link)
    end
  rescue
    raise "expected #{current_link} to point to a file, but it didn't -- wtf"
  end
  
  def stage(jar_location)
    say "Staging #{app} into #{root} (takes about a minute)"
    cptemp(jar_location, :user => :bouncer) do |path|
      FileUtils.cp(path, root)
      path
    end
  end
  
  def install(jar_location)
    say "Installing new #{app} into #{current_link}"
    sudo do
      FileUtils.rm_f(current_link)
      FileUtils.ln_sf(deploy_path(jar_location), current_link)
    end
  end
  
  def restart
    say "Restarting #{app} (takes about 2 minutes)"
    sudo do
      %x{ /etc/init.d/#{app} restart }
    end
  end
  
  def run(jar_location)
    stage(jar_location)
    install(jar_location)
    restart
  end
end
