# 智能数据分析平台

> 作者：[墨染青衣](https://github.com/asdrfdc)

## 项目介绍 📢
本项目是基于Spring Boot+RabbitMQ+AIGC的智能数据分析平台。

> AIGC ：Artificial Intelligence Generation Content(AI 生成内容)

区别于传统的数据分析方法，数据分析者只需要导入最原始的数据集，输入想要进行分析的目标，就能利用AI自动生成一个符合要求的图表以及分析结论。此外，还会有图表管理、异步生成、AI对话等功能。只需输入分析目标、原始数据和原始问题，利用AI就能一键生成可视化图表、分析结论和问题解答，大幅降低人工数据分析成本。

## 项目技术栈和特点 ❤️‍🔥
### 后端
1. Spring Boot 2.7.2
2. MyBatis + MyBatis Plus 
5. Spring AOP 切面编程
6. Spring Scheduler 定时任务
8. Redis：Redisson限流控制
11. **RabbitMQ：消息队列**
12. 智谱AI接口调用
13. JUC线程池异步化
14. JMX系统管理
15. Guava retrying重试技术
16. Swagger + Knife4j 项目文档
17. Easy Excel：表格数据处理、Hutool工具库 、Apache Common Utils、Lombok 注解


### 项目特性
- Spring Session Redis 分布式登录
- 基于JUC和RabbitMQ延迟队列实现超时中断
- 基于JMX实现系统检测，根据系统负载选择使用同步调用还是异步调用
- 基于JMX和定时任务实现反向压力，根据系统负载动态调整自定义线程池参数
- 基于Guava retrying实现重试机制
- 基于智谱AI实现智能数据分析
- 基于Easy Excel实现Excel转csv数据压缩 

### 项目功能 🎊
1. 通过上传excel表格并说明分析诉求，可自动生成数据报表与分析结果进行展示

## 项目背景 📖
1. 基于AI快速发展的时代，AI + 程序员 = 无限可能。
2. 传统数据分析流程繁琐：传统的数据分析过程需要经历繁琐的数据处理和可视化操作，耗时且复杂。
3. 技术要求高：传统数据分析需要数据分析者具备一定的技术和专业知识，限制了非专业人士的参与。
4. 人工成本高：传统数据分析需要大量的人力投入，成本昂贵。
5. AI自动生成图表和分析结论：该项目利用AI技术，只需导入原始数据和输入分析目标，即可自动生成符合要求的图表和分析结论。
6. 提高效率降低成本：通过项目的应用，能够大幅降低人工数据分析成本，提高数据分析的效率和准确性。


## 项目核心亮点 ⭐
1. 自动化分析：通过AI技术，将传统繁琐的数据处理和可视化操作自动化，使得数据分析过程更加高效、快速和准确。
3. 可视化管理：项目提供了图表管理功能，可以对生成的图表进行整理、保存和分享，方便用户进行后续的分析和展示。
4. 异步生成：项目支持异步生成，即使处理大规模数据集也能保持较低的响应时间，提高用户的使用体验和效率。

## 项目架构图 🔥
### 同步处理
系统空闲时，客户端输入分析诉求和原始数据，向业务后端发送请求。业务后端利用AI服务处理客户端数据，保持到数据库，并生成图表。处理后的数据由业务后端发送给AI服务，AI服务生成结果并返回给后端，最终将结果同步返回给客户端展示。


### 异步化处理
系统繁忙时，客户端提交任务，系统选择异步化处理，先保存基本数据到数据库返回id，用户可以去做别的事不用闲等，后台异步调用AI进行数据生成
#### JUC异步线程
利用线程池实现异步处理
#### RabbitMQ
利用消息队列实现异步处理，分为工作消息队列——执行任务，和延迟队列——实现超时中断






