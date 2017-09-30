package com.netto.demo.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.netto.client.bean.ReferenceBean;
import com.netto.client.router.ServiceRouterFactory;
import com.netto.context.ServiceAddress;
import com.netto.context.ServiceAddressGroup;
import com.netto.demo.Book;
import com.netto.demo.HelloService;
import com.netto.demo.User;
import com.netto.schedule.IScheduleTaskProcess;
import com.netto.schedule.ScheduleParam;
import com.netto.server.desc.ServiceDescApi;
import com.netto.server.desc.MethodDesc;

public class NettoClient {
    private static ServiceRouterFactory routerFactory;

    public static void main(String[] args) throws Exception {
        nginx_schedule();
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }

    public static void nginx_schedule() throws Exception {
        routerFactory = new ServiceRouterFactory();
        routerFactory.setRegistry("http://127.0.0.1:8330/api/");
        routerFactory.setServiceApp("myservice");
        routerFactory.setServiceGroup("*");
        routerFactory.afterPropertiesSet();
        ReferenceBean refer = new ReferenceBean();
        refer.setServiceName("helloSchedule");
        refer.setInterfaceClazz(IScheduleTaskProcess.class);
        refer.setTimeout(20 * 1000);
        refer.setProtocol("http");
        refer.setRouter(routerFactory.getObject());
        
        IScheduleTaskProcess process  = (IScheduleTaskProcess) refer.getObject();
        ScheduleParam param = new ScheduleParam();
        param.setClientThreadCount(5);
        param.setDataRetryCount(3);
        param.setExecuteCount(5);
        param.setFetchCount(100);
        param.setRetryTimeInterval(2);
        param.setServerArg("abc:cdt");
        
        int a = process.execute(param, 2);
        System.out.println(a);
    }

    public static void nginx_http_tcp() throws Exception {

        routerFactory = new ServiceRouterFactory();
        routerFactory.setRegistry("http://192.168.2.38:8330/api/");
        routerFactory.setServiceApp("myservice");
        routerFactory.setServiceGroup("*");
        routerFactory.afterPropertiesSet();
        ReferenceBean refer = new ReferenceBean();
        refer.setServiceName("helloService");
        refer.setRouter(routerFactory.getObject());
        refer.setInterfaceClazz(HelloService.class);
        refer.setTimeout(20 * 1000);
        refer.setProtocol("http");

        System.out.println("nginx_http_tcp begin-----------");
        HelloService helloProxy = (HelloService) refer.getObject();
        String res = helloProxy.sayByeBye(8);
        res = helloProxy.sayHello("netto");
        System.out.println(res);
        Map<String, List<User>> users = new HashMap<String, List<User>>();
        users.put("abc", new ArrayList<User>());
        User u = new User();
        u.setName("test1");
        u.setAge(10);
        List<Book> books = new ArrayList<Book>();
        Book book = new Book();
        book.setName("人类简史");
        books.add(book);
        u.setBooks(books);
        users.get("abc").add(u);
        List<User> list = helloProxy.saveUsers(users);
        // List<User> list = helloProxy.updateUsers(users.get("abc"));
        for (User u1 : list) {
            System.out.println("name=" + u1.getName() + ",age=" + u1.getAge());
        }
        System.out.println("nginx_http_tcp end-----------");
    }

    public static void nginx_tcp() throws Exception {
        // ServiceAddressGroup serverGroup = new ServiceAddressGroup();
        // serverGroup.setRegistry("http://127.0.0.1:8330/api/");
        // serverGroup.setServiceApp("myservice");
        // serverGroup.setServiceGroup("*");

        routerFactory = new ServiceRouterFactory();
        routerFactory.setRegistry("http://127.0.0.1:8330/api/");
        routerFactory.setServiceApp("myservice");
        routerFactory.setServiceGroup("*");
        routerFactory.afterPropertiesSet();
        routerFactory.setNeedSignature(true);
        ReferenceBean refer = new ReferenceBean();
        refer.setServiceName("helloService");
        refer.setRouter(routerFactory.getObject());
        refer.setInterfaceClazz(HelloService.class);
        refer.setTimeout(2000 * 1000);
        refer.setProtocol("tcp");
        System.out.println("nginx_tcp begin-----------");
        HelloService helloProxy = (HelloService) refer.getObject();
        String res = helloProxy.sayByeBye(8);
        res = helloProxy.sayHello("netto");
        System.out.println(res);
        Map<String, List<User>> users = new HashMap<String, List<User>>();
        users.put("abc", new ArrayList<User>());
        User u = new User();
        u.setName("test1");
        u.setAge(10);
        List<Book> books = new ArrayList<Book>();
        Book book = new Book();
        book.setName("人类简史");
        books.add(book);
        u.setBooks(books);
        users.get("abc").add(u);
        List<User> list = helloProxy.saveUsers(users);
        // List<User> list = helloProxy.updateUsers(users.get("abc"));
        for (User u1 : list) {
            System.out.println("name=" + u1.getName() + ",age=" + u1.getAge());
        }
        System.out.println("nginx_tcp end-----------");
    }

    public static void local_tcp() throws Exception {
        List<ServiceAddress> servers = new ArrayList<ServiceAddress>();
        ServiceAddress address = new ServiceAddress();
        address.setIp("localhost");
        address.setPort(9229);
        servers.add(address);

        ServiceAddressGroup serverGroup = new ServiceAddressGroup();
        serverGroup.setRegistry(null);
        serverGroup.setServiceApp("myservice");
        serverGroup.setServiceGroup("*");
        serverGroup.setServers(servers);

        routerFactory = new ServiceRouterFactory();

        routerFactory.setServiceApp("myservice");
        routerFactory.setServiceGroup("*");
        routerFactory.setServers("127.0.0.1:9229");

        routerFactory.afterPropertiesSet();

        ReferenceBean refer = new ReferenceBean();
        refer.setServiceName("helloService");
        refer.setRouter(routerFactory.getObject());
        refer.setInterfaceClazz(HelloService.class);
        refer.setTimeout(2000 * 1000);
        refer.setProtocol("tcp");

        System.out.println("local_tcp begin-----------");
        HelloService helloProxy = (HelloService) refer.getObject();
        String res = helloProxy.sayHello("netto");
        System.out.println(res);
        Map<String, List<User>> users = new HashMap<String, List<User>>();
        users.put("abc", new ArrayList<User>());
        User u = new User();
        u.setName("test1");
        u.setAge(10);
        List<Book> books = new ArrayList<Book>();
        Book book = new Book();
        book.setName("{\"name\":\"test\"}");
        books.add(book);
        u.setBooks(books);
        users.get("abc").add(u);
        List<User> list = helloProxy.saveUsers(users);
        // List<User> list = helloProxy.updateUsers(users.get("abc"));
        for (User u1 : list) {
            System.out.println("name=" + u1.getName() + ",age=" + u1.getAge());
        }
        System.out.println("local_tcp end-----------");

        System.out.println("service desc api begin------------");

        refer = new ReferenceBean();
        refer.setServiceName("$serviceDesc");
        refer.setRouter(routerFactory.getObject());
        refer.setInterfaceClazz(ServiceDescApi.class);
        refer.setTimeout(2 * 1000);
        refer.setProtocol("tcp");

        ServiceDescApi descObj = (ServiceDescApi) refer.getObject();
        Set<String> services = descObj.findServices();
        for (String service : services) {
            System.out.println(service);
        }
        List<MethodDesc> methodDecss = descObj.findServiceMethods("helloService");
        for (MethodDesc desc : methodDecss) {
            System.out.println(desc);
        }
        System.out.println("service desc api end------------");
    }

}
