# SimpleCloudStorage

SimpleCloudStorage is a simple cloud storage service that allows users to upload and download server's files.

## Installation

1. Install the required Java Development Kit (JDK) version 17 or higher.  
   I suggest using [Jetbrains Runtime 17.0.9](https://github.com/JetBrains/JetBrainsRuntime/tree/jdk-17.0.9%2B7)
   that is the version I used to develop this project.
2. Download the latest release from the [releases page](https://github.com/littlexfish/SimpleCloudStorage/releases),
   and extract the zip file.
3. Edit the `config.json` file in `SimpleCloudStorage` directory to configure the server.
4. Run the `SimpleCloudStorage.jar` file.
   ```shell
   java -jar SimpleCloudStorage.jar
   ```
5. Open your browser and navigate to `http://localhost:8080`(Default) to access the web interface.

## Environment Variables

- `SCS_HOME`: The directory where the config file is stored. (default is `SimpleCloudStorage` directory)
- `SCS_ROOT`: The directory where the files are stored. (default is the directory where the jar file is located)
- `SCS_WEB_STATIC`: The directory where the web interface files are stored. (default is `SCS_HOME/static`)

## Configuration

- `blacklistPath`: Blacklist of paths
  - `Array<String>`
  - Default: `[]`
- `listReverse`: Reverse the list, blacklisted paths will be allowed
  - `Boolean`
  - Default: `false`
- `allowedPreviewExtensions`: Allowed preview extensions
  - `Array<String>`
  - Default: `["jpg","jpeg","png","gif","bmp","webp","txt","log","pdf","zip"]`
- `maxPreviewFileSize`: Max file preview length in bytes,
  if the file is larger than this value,
  the preview for text file will be truncated or
  not be generated if the file is not a text file.
  - `Integer`
  - Default: `1024*1024` = 1MB

## Notice

This project is still in development,
 and I didn't develop account management yet,
 you should not open this server to the public network.

## Build

1. Clone the repository.
   (there are two repositories, one for the
   [frontend](https://github.com/littlexfish/simple-cloud-storage-web)
   and the other for the backend)
2. Build the backend project.
   ```shell
   cd SimpleCloudStorage
   ./gradlew buildFatJar
   ```
   And the server jar file will be in `SimpleCloudStorage/build/libs`.
3. Copy the jar file to the server and create `SimpleCloudStorage` directory in the same directory as the jar file.
4. Build the frontend project.
   ```shell
   cd simple-cloud-storage-web
   npm install
   npm run build
   ```
5. Copy the all `dist` inside files to the server static directory(default is `SimpleCloudStorage/static`).
6. Run the server jar file.
   ```shell
   java -jar SimpleCloudStorage.jar
   ```
7. Optionally, you can configure the server by editing the `config.json` file in the `SimpleCloudStorage` directory and restart the server.

## License

GNU General Public License v3.0

## Libraries

### Frontend

- [ReactJS](https://reactjs.org/)
- [Font Awesome](https://fontawesome.com/)
- [PatternFly](https://www.patternfly.org/)

### Backend

- [Ktor](https://ktor.io/)
    and its dependencies and plugins

