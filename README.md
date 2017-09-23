# tools
日常开发中写的一些小工具，基于java的springboot。

```shell
├── tool-amap                       // 高德地图web接口批量调用工具  
├── tool-coomon                     // 通用配置
├── .gitignore                      // git 忽略项
├── pom.xml                         // maven 配置文件
└── README.md                       // 项目说明文件
```

### tool-amap
功能需求：给了一批经纬度坐标，批量获取任意两点之间的驾车路径。

非功能需求：需要控制并发请求数

实现思路：
* 路径获取调用高德的web服务：http://lbs.amap.com/api/webservice/guide/api/direction
* 使用线程池进行网络请求，由于请求有时会出错，所以需要做重试
* 主线程通过sleep控制并发请求数

可以学习的点：
* lamada表达式
* 线程池的使用，execute、submit的使用