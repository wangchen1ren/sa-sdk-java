package com.sensorsdata.analytics.javasdk;

import com.sensorsdata.analytics.javasdk.bean.IDMEventRecord;
import com.sensorsdata.analytics.javasdk.bean.SensorsAnalyticsIdentity;
import com.sensorsdata.analytics.javasdk.consumer.DebugConsumer;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Id-Mapping track 相关接口单元测试 版本支持 3.3.0+
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2021/11/18 23:33
 */
public class IDMappingTrackTest extends SensorsBaseTest {

    /** 校验 ID_Mapping trackById 接口,只传入单维度用户属性 期望生成 IDM3.0 数据格式，identities 节点数据和传入的用户维度信息保持一致 */
    @Test
    public void checkIdMappingEvent() throws InvalidArgumentException {
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder().addIdentityProperty("login_id", "123").build();
        Map<String, Object> properties = new HashMap<>();
        properties.put("test", "test");
        properties.put("$project", "abc");
        properties.put("$token", "123");
        sa.trackById(identity, "test", properties);
        assertIDM3EventData(data);
        Assertions.assertEquals("test", data.get("event"));
        // 在 3.4.1 及以下版本 distinct_id 生成策略都是维度集合中 $identity_login_id 对应的值或者第一个标识对应的值
        // Assertions.assertEquals("123", data.get("distinct_id"));
        // 3.4.2 版本以上 distinct_id 生成策略调整
        Assertions.assertEquals("login_id+123", data.get("distinct_id"));

        Map<String, Object> prop = (Map<String, Object>) data.get("properties");
        Assertions.assertFalse(data.containsKey("$is_login_id"));
        Assertions.assertNotNull(data.get("properties"));
        Assertions.assertNotNull(data.get("project"));
        Assertions.assertNotNull(data.get("token"));
    }

    /** 校验 ID_Mapping trackById 接口,只传入多维度用户属性 期望生成 IDM3.0 数据格式，identities 节点数据和传入的用户维度信息保持一致 */
    @Test
    public void checkIdMappingTrackMoreId() throws InvalidArgumentException {
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder()
                        .addIdentityProperty("$identity_mobile", "123")
                        .addIdentityProperty("$identity_email", "fz@163.com")
                        .build();
        sa.trackById(identity, "view", null);
        assertIDM3EventData(data);
        Assertions.assertTrue(data.get("identities") instanceof Map);
        Map<?, ?> result = (Map<?, ?>) data.get("identities");
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("123", result.get("$identity_mobile"));
        Assertions.assertEquals("fz@163.com", result.get("$identity_email"));
    }

    /** 校验 ID_Mapping bind 接口,只传入多维度用户属性 期望生成 IDM3.0 数据格式，identities 节点数据和传入的用户维度信息保持一致 */
    @Test
    public void checkIdMappingBind() throws InvalidArgumentException {
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder()
                        .addIdentityProperty("$identity_mobile", "123")
                        .addIdentityProperty("$identity_email", "fz@163.com")
                        .build();
        sa.bind(identity);
        assertIDM3EventData(data);
        Assertions.assertTrue(data.get("identities") instanceof Map);
        Map<?, ?> result = (Map<?, ?>) data.get("identities");
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("123", result.get("$identity_mobile"));
        Assertions.assertEquals("fz@163.com", result.get("$identity_email"));
        Assertions.assertEquals("$BindID", data.get("event"));
        Assertions.assertEquals("track_id_bind", data.get("type"));
    }

    /** 校验 ID_Mapping bind 接口,只传入单维度用户属性 期望抛出参数不合法异常 */
    @Test
    public void checkIdMappingBindOneId() {
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder()
                        .addIdentityProperty("$identity_mobile", "123")
                        .build();
        try {
            sa.bind(identity);
            Assertions.fail();
        } catch (InvalidArgumentException e) {
            Assertions.assertTrue(true);
        }
    }

    /** 校验 ID_Mapping unbind 接口,最终数据格式 期望生成的数据结构中包含预期节点属性 */
    @Test
    public void checkUnbindUserId() throws InvalidArgumentException {
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder()
                        .addIdentityProperty("id_test1", "id_value1")
                        .build();
        sa.unbind(identity);
        assertIDM3EventData(data);
        Assertions.assertTrue(data.get("identities") instanceof Map);
        Map<?, ?> result = (Map<?, ?>) data.get("identities");
        Assertions.assertEquals("id_value1", result.get("id_test1"));
        Assertions.assertEquals("track_id_unbind", data.get("type"));
    }

    /** 校验 ID_Mapping trackById 接口,设置公共属性，最终事件中是否存在公共属性 期望生成的数据结构中存在公共属性 */
    @Test
    public void checkTrackByIdSuperProperties() throws InvalidArgumentException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("asd", "123");
        sa.registerSuperProperties(properties);
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder()
                        .addIdentityProperty("id_test1", "id_value1")
                        .build();
        sa.trackById(identity, "eee", null);
        Assertions.assertTrue(data.get("properties") instanceof Map);
        Map<?, ?> result = (Map<?, ?>) data.get("properties");
        Assertions.assertEquals("123", result.get("asd"));
    }

    /** 校验 ID_Mapping bind 接口,设置公共属性，最终事件中是否存在公共属性 期望生成的数据结构中存在公共属性 */
    @Test
    public void checkBindSuperProperties() throws InvalidArgumentException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("asd", "123");
        sa.registerSuperProperties(properties);
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder()
                        .addIdentityProperty("id_test1", "id_value1")
                        .addIdentityProperty("eee", "123")
                        .build();
        sa.bind(identity);
        Assertions.assertTrue(data.get("properties") instanceof Map);
        Map<?, ?> result = (Map<?, ?>) data.get("properties");
        Assertions.assertEquals("123", result.get("asd"));
    }

    /** 校验 ID_Mapping unbind 接口,设置公共属性，最终事件中是否存在公共属性 期望生成的数据结构中存在公共属性 */
    @Test
    public void checkUnbindSuperProperties() throws InvalidArgumentException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("asd", "123");
        sa.registerSuperProperties(properties);
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder()
                        .addIdentityProperty("id_test1", "id_value1")
                        .build();
        sa.unbind(identity);
        Assertions.assertTrue(data.get("properties") instanceof Map);
        Map<?, ?> result = (Map<?, ?>) data.get("properties");
        Assertions.assertEquals("123", result.get("asd"));
    }

    /**
     * 用户维度标识携带 $identity_login_id 再调用 trackById 接口。生成事件； 期望生成的数据结构中 distinct_id 为
     * $identity_login_id 对应的 value
     */
    @Test
    public void checkTrackByIdWithLoginId() throws InvalidArgumentException {
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder()
                        .addIdentityProperty(SensorsAnalyticsIdentity.LOGIN_ID, "fz123")
                        .addIdentityProperty(SensorsAnalyticsIdentity.EMAIL, "fz@163.com")
                        .build();
        sa.trackById(identity, "test", null);
        assertIDM3EventData(data);
        assertDataLib(data.get("lib"));
        Assertions.assertEquals("fz123", data.get("distinct_id"));
    }

    // ----------------------3.4.2--------------------------

    /**
     * 用户维度标识不携带 $identity_login_id 再调用 trackById 接口。生成事件； 期望生成的数据结构中 distinct_id 为维度集合中第一个标识
     * key+value
     */
    @Test
    public void checkTrackByIdWithoutLoginId() throws InvalidArgumentException {
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder()
                        .addIdentityProperty("login_id", "fz123")
                        .addIdentityProperty(SensorsAnalyticsIdentity.EMAIL, "fz@163.com")
                        .build();
        sa.trackById(identity, "test", null);
        assertIDM3EventData(data);
        assertDataLib(data.get("lib"));
        Assertions.assertEquals("login_id+fz123", data.get("distinct_id"));
    }

    /**
     * 用户维度标识携带 $identity_login_id,并且指定 distinctId; 使用 IDMEventRecord 调用 trackById 接口。生成事件；
     * 期望生成的数据结构中 distinct_id 为指定的 distinctId 对应的 value
     */
    @Test
    public void checkEventRecordWithLoginId() throws InvalidArgumentException {
        IDMEventRecord eventRecord =
                IDMEventRecord.starter()
                        .setDistinctId("zzz")
                        .addIdentityProperty(SensorsAnalyticsIdentity.LOGIN_ID, "fz123")
                        .addIdentityProperty(SensorsAnalyticsIdentity.EMAIL, "fz@163.com")
                        .setEventName("test")
                        .addProperty("eee", "rrr")
                        .build();
        sa.trackById(eventRecord);
        assertIDM3EventData(data);
        Assertions.assertEquals("zzz", data.get("distinct_id"));
    }

    /**
     * 用户维度标识不携带 $identity_login_id 使用 IDMEventRecord 调用 trackById 接口。生成事件； 期望生成的数据结构中 distinct_id
     * 为多维度标识集合中第一个
     */
    @Test
    public void checkEventRecordWithoutLoginId() throws InvalidArgumentException {
        IDMEventRecord eventRecord =
                IDMEventRecord.starter()
                        .addIdentityProperty("login_id", "fz123")
                        .addIdentityProperty(SensorsAnalyticsIdentity.EMAIL, "fz@163.com")
                        .setEventName("test")
                        .addProperty("eee", "rrr")
                        .build();
        sa.trackById(eventRecord);
        assertIDM3EventData(data);
        Assertions.assertEquals("login_id+fz123", data.get("distinct_id"));
    }

    /**
     * 用户维度标识不携带 $identity_login_id，传入 distinctId; 使用 IDMEventRecord 调用 trackById 接口。生成事件；
     * 期望生成的数据结构中 distinct_id 为 distinctId 对应的 value
     */
    @Test
    public void checkEventRecordWithoutLoginId1() throws InvalidArgumentException {
        IDMEventRecord eventRecord =
                IDMEventRecord.starter()
                        .setDistinctId("zzz")
                        .addIdentityProperty("login_id", "fz123")
                        .addIdentityProperty(SensorsAnalyticsIdentity.EMAIL, "fz@163.com")
                        .setEventName("test")
                        .addProperty("eee", "rrr")
                        .build();
        sa.trackById(eventRecord);
        assertIDM3EventData(data);
        Assertions.assertEquals("zzz", data.get("distinct_id"));
    }

    /**
     * 用户维度标识携带 $identity_login_id 使用 IDMEventRecord 调用 bind 接口。生成事件； 期望生成的数据结构中 distinct_id 为
     * $identity_login_id 对应的 value
     */
    @Test
    public void checkEventRecordBindWithLoginId() throws InvalidArgumentException {
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder()
                        .addIdentityProperty(SensorsAnalyticsIdentity.EMAIL, "fz@163.com")
                        .addIdentityProperty(SensorsAnalyticsIdentity.LOGIN_ID, "fz123")
                        .build();
        sa.bind(identity);
        assertIDM3EventData(data);
        Assertions.assertEquals("fz123", data.get("distinct_id"));
    }

    /**
     * 用户维度标识不携带 $identity_login_id 使用 IDMEventRecord 调用 bind 接口。生成事件； 期望生成的数据结构中 distinct_id
     * 为多维度集合中多第一个标识 key+value
     */
    @Test
    public void checkEventRecordBindWithoutLoginId() throws InvalidArgumentException {
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder()
                        .addIdentityProperty("login_id", "fz123")
                        .addIdentityProperty(SensorsAnalyticsIdentity.EMAIL, "fz@163.com")
                        .build();
        sa.bind(identity);
        assertIDM3EventData(data);
        Assertions.assertEquals("login_id+fz123", data.get("distinct_id"));
    }

    /**
     * 用户维度标识携带 $identity_login_id 使用 IDMEventRecord 调用 unbind 接口。生成事件； 期望生成的数据结构中 distinct_id 为
     * $identity_login_id 对应的 value
     */
    @Test
    public void checkEventRecordUnbindWithLoginId() throws InvalidArgumentException {
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder()
                        .addIdentityProperty(SensorsAnalyticsIdentity.LOGIN_ID, "fz123")
                        .build();
        sa.unbind(identity);
        assertIDM3EventData(data);
        Assertions.assertEquals("fz123", data.get("distinct_id"));
    }

    /**
     * 用户维度标识不携带 $identity_login_id 使用 IDMEventRecord 调用 unbind 接口。生成事件； 期望生成的数据结构中 distinct_id
     * 为多维度集合中多第一个标识 key+value
     */
    @Test
    public void checkEventRecordUnbindWithoutLoginId() throws InvalidArgumentException {
        SensorsAnalyticsIdentity identity =
                SensorsAnalyticsIdentity.builder().addIdentityProperty("login_id", "fz123").build();
        sa.unbind(identity);
        assertIDM3EventData(data);
        Assertions.assertEquals("login_id+fz123", data.get("distinct_id"));
    }

    /**
     * 用户维度标识携带 $project,$token,$time 使用 IDMEventRecord 调用 trackById 接口。生成事件； 期望生成的数据结构中，外层数据携带
     * project,token,time 节点；内层 properties 不携带相关参数
     */
    @Test
    public void checkEventRecordWithPresetProperty() throws InvalidArgumentException {
        Date time = new Date();
        IDMEventRecord eventRecord =
                IDMEventRecord.starter()
                        .addIdentityProperty("login_id", "fz123")
                        .addIdentityProperty(SensorsAnalyticsIdentity.EMAIL, "fz@163.com")
                        .setEventName("test")
                        .addProperty("$project", "test")
                        .addProperty("$token", "er4eee")
                        .addProperty("$time", time)
                        .build();
        sa.trackById(eventRecord);
        assertIDM3EventData(data);
        Assertions.assertEquals(time.getTime(), data.get("time"));
        Assertions.assertTrue(data.containsKey("project"));
        Assertions.assertEquals("test", data.get("project"));
        Assertions.assertTrue(data.containsKey("token"));
        Assertions.assertEquals("er4eee", data.get("token"));
        final Map<String, Object> properties = (Map<String, Object>) data.get("properties");
        Assertions.assertFalse(properties.containsKey("$project"));
        Assertions.assertFalse(properties.containsKey("$token"));
        Assertions.assertFalse(properties.containsKey("$time"));
    }

    /**
     * 用户构建的数据属性集合，使用 IDMEventRecord 调用 track 接口，SDK 处理之后并不删除用户的集合节点信息；
     * 期望生成的数据结构符合神策数据格式，并且外部的集合节点不改变
     */
    @Test
    public void checkTrackEventDontDeleteMapNode() throws InvalidArgumentException {
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put("hello", "fz");
        propertiesMap.put("$token", "er4eee");
        propertiesMap.put("$project", "test");
        IDMEventRecord eventRecord =
                IDMEventRecord.starter()
                        .addIdentityProperty("login_id", "fz123")
                        .addIdentityProperty(SensorsAnalyticsIdentity.EMAIL, "fz@163.com")
                        .setEventName("test")
                        .addProperty("haha", "test")
                        .addProperties(propertiesMap)
                        .build();
        sa.trackById(eventRecord);
        assertIDM3EventData(data);
        Assertions.assertTrue(propertiesMap.containsKey("hello"));
        Assertions.assertTrue(propertiesMap.containsKey("$token"));
        Assertions.assertTrue(propertiesMap.containsKey("$project"));
    }

    /** 用户构建的数据属性集合，使用 IDMEventRecord 调用 track 接口，使用 debug 模式上报； 期望生成的数据结构符合神策数据格式，并且服务端校验通过 */
    @Test
    public void checkTrackEventWithNet() throws InvalidArgumentException {
        SensorsAnalytics sensors = new SensorsAnalytics(new DebugConsumer(url, true));
        IDMEventRecord eventRecord =
                IDMEventRecord.starter()
                        .addIdentityProperty("login_id", "fz123")
                        .addIdentityProperty(SensorsAnalyticsIdentity.EMAIL, "fz@163.com")
                        .setEventName("test")
                        .addProperty("haha", "test")
                        .build();
        sensors.trackById(eventRecord);
    }
}
