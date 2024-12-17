package com.lzhphantom.lzhphantom_findfriendsbackend;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.github.javafaker.Faker;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.Tag;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.User;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.TagService;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.lzhphantom.lzhphantom_findfriendsbackend.constants.UserConstant.SALT;

@SpringBootTest
class LzhphantomFindFriendsBackendApplicationTests {

    private Faker faker;

    @Resource
    private TagService tagService;

    @Resource
    private UserService userService;
    @BeforeEach
    void setUp() {
        this.faker = new Faker(new Locale("zh_CN"));
    }

    @Test
    void contextLoads() {
    }

    @Test
    void addTag() {
        //添加编程语言
        Long parentId = 11L;
        List<Tag> list = CollUtil.newArrayList();
        List<String> tagNames = tagService.lambdaQuery().eq(Tag::getParentId, parentId).list()
                .stream().map(Tag::getTagName).collect(Collectors.toList());
        int batch = 10000;
        for (int i = 0; i < batch; i++) {
            Tag tag = new Tag();
            String name = faker.programmingLanguage().name();
            if (tagNames.contains(name)){
                continue;
            }
            tag.setTagName(name);
            tag.setUserId(1L);
            tag.setParentId(parentId);
            list.add(tag);
        }
        // 对list根据tagName去重
        // 对list根据tagName忽略大小写去重
        list = new ArrayList<>(list.stream()
                .collect(Collectors.toMap(
                        tag -> tag.getTagName().toLowerCase(),  // 将tagName转换为小写进行比较
                        tag -> tag,
                        (existing, replacement) -> existing))  // 保留第一个出现的元素
                .values());


        tagService.saveBatch(list);
    }
    private static String generatePersonalProfile(Faker faker) {
        return "姓名: " + faker.name().fullName() + "\n" +
                "性别: " + (faker.bool().bool() ? "男" : "女") + "\n" +
                "出生日期: " + faker.date().birthday().toString() + "\n" +
                "联系方式: " + faker.phoneNumber().cellPhone() + "\n" +
                "电子邮件: " + faker.internet().emailAddress() + "\n" +
                "现居地址: " + faker.address().fullAddress() + "\n" +
                "职业: " + faker.company().profession() + "\n" +
                "公司: " + faker.company().name() + "\n" +
                "职位: " + faker.company().bs() + "\n" +
                "个人简介: " + faker.lorem().paragraph(3) + "\n" +
                "技能: " + String.join(", ", faker.options().option("Java", "Python", "C++", "JavaScript", "Go")) + "\n" +
                "兴趣爱好: " + String.join(", ", faker.options().option("阅读", "旅行", "编程", "电影", "音乐")) + "\n";
    }

    @Test
    void insertUserByConcurrency() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int batchNum = 1000000;
        List<User> userList = CollUtil.newArrayList();
        for (int i = 0; i < batchNum; i++) {
            User user = new User();
            user.setUsername(faker.name().fullName());
            user.setLoginAccount("test" + i);
            user.setAvatarUrl("https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg");
            user.setGender(faker.bool().bool() ? 1 : 0);
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + "test@1234").getBytes());
            user.setPassword(encryptPassword);
            user.setPhone(faker.phoneNumber().phoneNumber());
            user.setEmail(faker.internet().emailAddress());
            ArrayList<String> tags = CollUtil.newArrayList();
            tags.add(faker.programmingLanguage().name());
            tags.add(faker.options().option("男", "女"));
            tags.add(faker.options().option("大一", "大二", "大三", "大四", "研究生", "博士"));
            user.setTags(JSONUtil.toJsonStr(tags));
            user.setProfile(faker.lorem().paragraph(3));
            userList.add(user);
        }
        // 将数据分成10组
        int numberOfGroups = 10;
        int groupSize = userList.size() / numberOfGroups;
        List<List<User>> groups = CollUtil.newArrayList();
        for (int i = 0; i < numberOfGroups; i++) {
            int fromIndex = i * groupSize;
            int toIndex = (i == numberOfGroups - 1) ? userList.size() : fromIndex + groupSize;
            groups.add(userList.subList(fromIndex, toIndex));
        }
        // 提交任务到线程池
        // 创建一个固定大小的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfGroups);
        List<CompletableFuture<Void>> futures = CollUtil.newArrayList();
        for (List<User> group : groups) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("test" + Thread.currentThread().getName());
                userService.saveBatch(group, 1000);
            }, executorService);
            futures.add(future);
        }
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        // 关闭线程池
        executorService.shutdown();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}
