# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed

## [1.2.1] - 2018-05-14
### Changed
- Increased max secrets size to 250KB.

### Added
- Secrets cli version check on server side.

## [1.2.0] - 2018-05-04
### Fixed
- AD authentication issue.
- `.json` and `.xml` secrets upload issue.
- Can't delete secrets with space in it.

### Changed
- New docker image.
- Applied Google code formatting
- Updated to latest dependencies
- Updated to latest swagger doc
- Updated java doc.
- Changed pom.xml to upload artifacts to Maven central


## [1.1.0] - 2017-09-05
### Fixed
- Misc bug fixes.

## [1.0.0] - 2017-08-10
### Added
- Initial release.

<!-- Releases -->

[Unreleased]: https://github.com/oneops/secrets-proxy/compare/release-1.2.1...HEAD
[1.2.1]: https://github.com/oneops/secrets-proxy/compare/1.2.0...release-1.2.1
[1.2.0]: https://github.com/oneops/secrets-proxy/compare/1.1.0...release-1.2.0
[1.1.0]: https://github.com/oneops/secrets-proxy/compare/1.0.0...1.1.0
[1.0.0]: https://github.com/oneops/secrets-proxy/compare/f6900edc7077c6052d6417ebf69c8481329cef77...1.0.0