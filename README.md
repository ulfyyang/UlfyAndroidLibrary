# UlfyAndroidLibrary

该仓库用于存放优菲框架的各个库模块，方便其它项目已子模块的方式源码集成。

# 注意事项

作为子仓库需要以ulfy-lib为目录名。

在settings.gradle中加入对应的项目。

因为模块依赖于config.gradle中的版本配置，因此需要把该文件也拷贝到项目中并应用。目前该文件放到库中，实际项目只需要写一下应用即可。

采用源码方式依赖对应的混淆可以由库提供

如果子仓库的目录不是ulfy-lib，需要全局替换模块间的依赖，全局搜索ulfy-lib替换为自己的目录。
