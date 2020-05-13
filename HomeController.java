package jp.co.ana.cas.proto.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Home redirection to OpenAPI api documentation
 */
@Controller
public class HomeController {

    @RequestMapping("/")
    public String index() {
        // FIXME: 適切な戻り値に修正
        return "redirect:swagger-ui.html";
    }

    @Scheduled(fixedRate = 1000)
    public void task1() {
        //処理
	System.out.println("HomeConroller.task1()");
    }
}
