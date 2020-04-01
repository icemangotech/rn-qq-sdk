require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "rn-qq-sdk"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  rn-qq-sdk
                   DESC
  s.homepage     = "https://github.com/icemangotech/rn-qq-sdk"
  s.license    = { :type => "BSD-3-Clause", :file => "LICENSE" }
  s.authors      = { "phecda" => "phecda@brae.co" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/icemangotech/rn-qq-sdk.git", :tag => "v#{s.version}" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true

  s.vendored_frameworks = 'ios/RCTQQSDK/TencentOpenAPI.framework'
  s.resource  = 'ios/RCTQQSDK/TencentOpenApi_IOS_Bundle.bundle'

  s.framework = 'SystemConfiguration','CoreGraphics','CoreTelephony'
  s.libraries = 'iconv','sqlite3','stdc++','z'

  s.dependency "React"
  # ...
  # s.dependency "..."
end

