package com.xuecheng.search.config;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 课程索引初始化器：服务启动后检查 course-publish 索引是否存在，不存在则按预定义 mapping 创建。
 * 解决问题：若让 ES 依据第一条文档动态推断 mapping，createDate 会被猜成 text、
 * mtName 聚合会因 text 字段禁用 fielddata 而报错，导致检索接口 500。
 */
@Slf4j
@Component
public class CourseIndexInitializer implements ApplicationRunner {

    @Autowired
    RestHighLevelClient client; // ES 高级客户端，由 ElasticsearchConfig 注入

    @Value("${elasticsearch.course.index}")
    private String courseIndexStore; // 索引名，来自 Nacos 配置 elasticsearch.course.index

    /**
     * 索引 mapping 定义，与检索代码的用法严格对应：
     * - name/description: text，供 multi_match 全文检索与高亮
     * - mtName/stName: keyword 主字段（terms 聚合用）+ keyword 子字段（termQuery "mtName.keyword" 过滤用）
     * - mt/st/grade/teachmode/charge/status: keyword，精确过滤
     * - createDate: date，显式指定 yyyy-MM-dd HH:mm:ss 格式（与 CourseIndex 的 @JSONField 一致）
     */
    private static final String MAPPING_JSON = "{\n" +
            "  \"properties\": {\n" +
            "    \"id\":            {\"type\": \"long\"},\n" +
            "    \"companyId\":     {\"type\": \"long\"},\n" +
            "    \"companyName\":   {\"type\": \"keyword\"},\n" +
            "    \"name\":          {\"type\": \"text\"},\n" +
            "    \"description\":   {\"type\": \"text\"},\n" +
            "    \"users\":         {\"type\": \"text\"},\n" +
            "    \"tags\":          {\"type\": \"text\"},\n" +
            "    \"mt\":            {\"type\": \"keyword\"},\n" +
            "    \"mtName\":        {\"type\": \"keyword\", \"fields\": {\"keyword\": {\"type\": \"keyword\"}}},\n" +
            "    \"st\":            {\"type\": \"keyword\"},\n" +
            "    \"stName\":        {\"type\": \"keyword\", \"fields\": {\"keyword\": {\"type\": \"keyword\"}}},\n" +
            "    \"grade\":         {\"type\": \"keyword\"},\n" +
            "    \"teachmode\":     {\"type\": \"keyword\"},\n" +
            "    \"pic\":           {\"type\": \"keyword\", \"index\": false},\n" +
            "    \"status\":        {\"type\": \"keyword\"},\n" +
            "    \"remark\":        {\"type\": \"text\"},\n" +
            "    \"charge\":        {\"type\": \"keyword\"},\n" +
            "    \"price\":         {\"type\": \"float\"},\n" +
            "    \"originalPrice\": {\"type\": \"float\"},\n" +
            "    \"validDays\":     {\"type\": \"integer\"},\n" +
            "    \"createDate\":    {\"type\": \"date\", \"format\": \"yyyy-MM-dd HH:mm:ss\"}\n" +
            "  }\n" +
            "}";

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 1. 判断索引是否已存在，存在则跳过（保证可重复启动，幂等）
            boolean exists = client.indices().exists(new GetIndexRequest(courseIndexStore), RequestOptions.DEFAULT);
            if (exists) {
                log.info("课程索引 [{}] 已存在，跳过初始化", courseIndexStore);
                return;
            }
            // 2. 不存在则按预定义 mapping 创建索引
            CreateIndexRequest request = new CreateIndexRequest(courseIndexStore);
            request.mapping(MAPPING_JSON, XContentType.JSON);
            client.indices().create(request, RequestOptions.DEFAULT);
            log.info("课程索引 [{}] 创建成功", courseIndexStore);
        } catch (Exception e) {
            // ES 暂不可用时只记录错误，不阻断服务启动（启动顺序上 ES 可能晚于本服务就绪）
            log.error("初始化课程索引 [{}] 失败，请检查 Elasticsearch 是否可用", courseIndexStore, e);
        }
    }
}
