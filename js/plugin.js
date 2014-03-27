
var Docker = (function(Docker) {

  Docker.pluginName = "docker";
  Docker.log = Logger.get(Docker.pluginName);
  Docker.templatePath = "../hawtio-dockerui/partials/";

  // hmm, took out ngRoute
  angular.module(Docker.pluginName, ['hawtioCore', 'dockerui.services', 'dockerui.filters'])
      .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/docker', {templateUrl: Docker.templatePath + 'dashboard.html', controller: 'DashboardController'});
        $routeProvider.when('/containers/', {templateUrl: Docker.templatePath + 'containers.html', controller: 'ContainersController'});
        $routeProvider.when('/containers/:id/', {templateUrl: Docker.templatePath + 'container.html', controller: 'ContainerController'});
        $routeProvider.when('/images/', {templateUrl: Docker.templatePath + 'images.html', controller: 'ImagesController'});
        $routeProvider.when('/images/:id/', {templateUrl: Docker.templatePath + 'image.html', controller: 'ImageController'});
        $routeProvider.when('/d_settings', {templateUrl: Docker.templatePath + 'settings.html', controller: 'SettingsController'});
      }])
    // This is your docker url that the api will use to make requests
    // You need to set this to the api endpoint without the port i.e. http://192.168.1.9
      .constant('DOCKER_ENDPOINT', '/hawtio-dockerui/dockerapi')
      .constant('DOCKER_PORT', '') // Docker port, leave as an empty string if no port is requred.  If you have a port, prefix it with a ':' i.e. :4243
      .constant('UI_VERSION', 'v0.4')
      .constant('DOCKER_API_VERSION', 'v1.8')
      .run(function(workspace, viewRegistry) {
        Docker.log.info("plugin running");

        Core.addCSS('../hawtio-dockerui/css/plugin.css');

        // tell hawtio that we have our own custom layout for
        // our view
        viewRegistry["docker"] = Docker.templatePath + "dockerLayout.html";
        viewRegistry["containers"] = Docker.templatePath + "dockerLayout.html";
        viewRegistry["images"] = Docker.templatePath + "dockerLayout.html";
        viewRegistry["d_settings"] = Docker.templatePath + "dockerLayout.html";

        // Add a top level tab to hawtio's navigation bar
        workspace.topLevelTabs.push({
          id: "docker",
          content: "Docker",
          title: "Docker UI",
          isValid: function(workspace) { return true },
          href: function() { return "#/docker"; },
          isActive: function() {
            return workspace.isLinkActive("docker")
                || workspace.isLinkActive("containers")
                || workspace.isLinkActive("images")
                || workspace.isLinkActive("d_settings");
          }
        });

      });


  Docker.MastheadController = function ($scope, workspace) {
    $scope.template = Docker.templatePath + 'mastheadPlugin.html';
    $scope.isActive = function (href) {
      return workspace.isLinkActive(href);
    };
  };

  Docker.StatusBarController = function ($scope, Settings) {
    $scope.template = Docker.templatePath + 'statusbar.html';

    $scope.uiVersion = Settings.uiVersion;
    $scope.apiVersion = Settings.version;
  };

  // TODO will need to see where this is included from
  Docker.SideBarController = function ($scope, Container, Settings) {
    $scope.template = Docker.templatePath + 'sidebar.html';
    $scope.containers = [];
    $scope.endpoint = Settings.endpoint;

    Container.query({all: 0}, function(d) {
      $scope.containers = d;
    });
  };

  return Docker;
}(Docker || {}));

hawtioPluginLoader.addModule('dockerui.services');
hawtioPluginLoader.addModule('dockerui.filters');
hawtioPluginLoader.addModule(Docker.pluginName);
