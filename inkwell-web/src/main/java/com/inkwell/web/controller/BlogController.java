package com.inkwell.web.controller;

import com.inkwell.web.client.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

/**
 * Public-facing MVC controller for the reader experience.
 * Renders Thymeleaf templates under /templates/blog/
 */
@Controller
@RequiredArgsConstructor
public class BlogController {

    private final PostClient         postClient;
    private final CommentClient      commentClient;
    private final CategoryClient     categoryClient;
    private final NotificationClient notificationClient;
    private final NewsletterClient   newsletterClient;

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "0")  int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        model.addAttribute("posts",        postClient.getPublishedPosts(page, size));
        model.addAttribute("categories",   categoryClient.getAllCategories());
        model.addAttribute("trendingTags", categoryClient.getTrendingTags());
        return "blog/home";
    }

    @GetMapping("/blog/{slug}")
    public String viewPost(@PathVariable String slug, Model model) {
        Map<String, Object> post = postClient.getPostBySlug(slug);
        if (post == null) return "redirect:/";
        Integer postId = (Integer) post.get("postId");
        model.addAttribute("post",     post);
        model.addAttribute("comments", commentClient.getThreadedComments(postId));
        try { postClient.incrementView(postId); } catch (Exception ignored) {}
        return "blog/post";
    }

    @GetMapping("/category/{slug}")
    public String viewCategory(@PathVariable String slug, Model model) {
        model.addAttribute("category", categoryClient.getCategoryBySlug(slug));
        model.addAttribute("posts",    postClient.getPostsByCategory(slug));
        return "blog/category";
    }

    @GetMapping("/tag/{slug}")
    public String viewTag(@PathVariable String slug, Model model) {
        model.addAttribute("tag",   categoryClient.getTagBySlug(slug));
        model.addAttribute("posts", postClient.getPostsByTag(slug));
        return "blog/tag";
    }

    @GetMapping("/search")
    public String searchPosts(@RequestParam String query, Model model) {
        model.addAttribute("query",   query);
        model.addAttribute("results", postClient.searchPosts(query));
        return "blog/search";
    }

    @PostMapping("/blog/{postId}/comment")
    public String addComment(@PathVariable Integer postId,
                             @RequestParam String content,
                             @RequestParam(required = false) Integer parentCommentId,
                             Principal principal,
                             RedirectAttributes ra) {
        commentClient.addComment(postId, content, parentCommentId, principal.getName());
        ra.addFlashAttribute("success", "Comment posted!");
        return "redirect:/blog/post/" + postId;
    }

    @PostMapping("/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Integer commentId,
                                @RequestParam Integer postId,
                                Principal principal) {
        commentClient.deleteComment(commentId, principal.getName());
        return "redirect:/blog/post/" + postId;
    }

    @PostMapping("/blog/{postId}/like")
    public String likePost(@PathVariable Integer postId) {
        postClient.likePost(postId);
        return "redirect:/blog/" + postId;
    }

    @PostMapping("/comment/{commentId}/like")
    public String likeComment(@PathVariable Integer commentId,
                              @RequestParam Integer postId) {
        commentClient.likeComment(commentId);
        return "redirect:/blog/post/" + postId;
    }

    @PostMapping("/newsletter/subscribe")
    public String subscribe(@RequestParam String email,
                            @RequestParam(required = false) String fullName,
                            RedirectAttributes ra) {
        newsletterClient.subscribe(email, fullName);
        ra.addFlashAttribute("success", "Check your email to confirm your subscription!");
        return "redirect:/";
    }

    @GetMapping("/notifications")
    public String viewNotifications(Principal principal, Model model) {
        model.addAttribute("notifications",
                notificationClient.getNotificationsForUser(principal.getName()));
        return "blog/notifications";
    }

    @PostMapping("/notifications/{notificationId}/read")
    public String markNotifRead(@PathVariable Integer notificationId) {
        notificationClient.markAsRead(notificationId);
        return "redirect:/notifications";
    }
}
