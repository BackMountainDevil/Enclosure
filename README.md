# Enclosure
CAU-农业信息化开发实训||-第四小分队

# Day Log
## Day1-初次分工
第四组共7人，主要任务：圈地（助教：BS）

成员一（ZZQ）：界面框架，实现同一页面的GPS\手绘圈地切换，（以切换稳定性、时效性作为加分标准）。
成员二（LZW）：地图调用，实现高德地图权限，地图面板定位、清除、截图等按钮功能，可与第三组交叉协作。

成员三（HSX）、六（YX）：GPS圈地，实现手机端移动采点功能，并实现封闭面积计算，实现地块信息的保存自动填入。
成员四（ZC）、七（YCR）：手绘圈地，实现手机端地图的面板点击圈地功能，并实现封闭面积计算，实现地块信息的保存自动填入。

成员一（ZZQ）、二（LZW）、** 五（ZXG）**：圈地信息的数据库导入保存，此为本软件重要功能，具体数据库实现要求同上-即实现数据库表单的本地或AS自带SQLite保存，实现模拟后台服务器数据库上传下载（加分）。可与其他相关功能人员交流协同开发。

## Day2-再次分工

D1成果总结：
1. 数据库：ZC在阿里云上的云服务器搭建MariaDB, ZXG实现数据库的上传
2. 地图： YCR实现地图的调用
今日：由于水平不足、实力差距悬殊，重新更新分工
- YCR：Git、Github使用教学，实现地图调用，实现高德地图权限，清除、切换模式等按钮功能，实现城市标准地图和卫星地图切换，手绘圈地获取坐标
- YX： 负责多边形面积的计算、高德定位权限和地图面板定位GPS打点的实现
- ZC：数据库搭建、界面框架设计、apk封面及Release
- ZXG：本地数据上传到Enclosure数据库，外加下载、上传成功与否的提示
- HSX：本地数据的sqlite暂存，在没有网络的时候暂存圈地数据，检测到网络的时候使用ZXG的方式上传
- ZZQ：截图功能在不同机型的统一实现
- LZW： 截图照片保存到相册以及整合到Enclosure项目

# References

[相关学习案例](https://github.com/BackMountainDevil/AndroidStudioLearn)
[SDK](https://lbs.amap.com/api/android-sdk/download/)
[入门指南](https://lbs.amap.com/api/android-location-sdk/gettingstarted/#creatproject)
[配置工程](https://lbs.amap.com/api/android-sdk/guide/create-project/android-studio-create-project)
[key](https://lbs.amap.com/api/android-location-sdk/guide/create-project/get-key/)