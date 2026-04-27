package com.inkwell.web.controller;

import com.inkwell.web.client.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin panel MVC controller.
 * All endpoints require ADMIN role.
 * Renders Thymeleaf templates under /templates/admin/
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AuthClient         authClient;
    private final PostClient         postClient;
    private final CommentClient      commentClient;
    private final CategoryClient     categoryClient;
    private final MediaClient        mediaClient;
    private final NewsletterClient   newsletterClient;
    private final NotificationClient notificationClient;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("totalUsers",       authClient.getTotalUserCount());
        model.addAttribute("totalPosts",       postClient.getTotalPublishedCount());
        model.addAttribute("totalSubscribers", newsletterClient.getSubscriberCount());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", authClient.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/{userId}/role")
    public String changeUserRole(@PathVariable Integer userId,
                                 @RequestParam String role, RedirectAttributes ra) {
        authClient.changeUserRole(userId, role);
        ra.addFlashAttribute("success", "User role updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/suspend")
    public String suspendUser(@PathVariable Integer userId, RedirectAttributes ra) {
        authClient.deactivateUser(userId);
        ra.addFlashAttribute("success", "User suspended.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/reactivate")
    public String reactivateUser(@PathVariable Integer userId, RedirectAttributes ra) {
        authClient.reactivateUser(userId);
        ra.addFlashAttribute("success", "User reactivated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Integer userId, RedirectAttributes ra) {
        authClient.deleteUser(userId);
        ra.addFlashAttribute("success", "User deleted.");
        return "redirect:/admin/users";
    }

    @GetMapping("/posts")
    public String manageAllPosts(Model model) {
        model.addAttribute("posts", postClient.getAllPosts());
        return "admin/posts";
    }

    @PostMapping("/posts/{postId}/feature")
    public String featurePost(@PathVariable Integer postId,
                              @RequestParam(defaultValue = "true") boolean featured,
                              RedirectAttributes ra) {
        postClient.featurePost(postId, featured);
        ra.addFlashAttribute("success", featured ? "Post featured." : "Post unfeatured.");
        return "redirect:/admin/posts";
    }

    @PostMapping("/posts/{postId}/delete")
    public String deletePost(@PathVariable Integer postId, RedirectAttributes ra) {
        postClient.adminDeletePost(postId);
        ra.addFlashAttribute("success", "Post deleted.");
        return "redirect:/admin/posts";
    }

    @GetMapping("/categories")
    public String manageCategories(Model model) {
        model.addAttribute("categories", categoryClient.getAllCategories());
        return "admin/categories";
    }

    @PostMapping("/categories")
    public String createCategory(@RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(required = false) Integer parentCategoryId,
                                 RedirectAttributes ra) {
        categoryClient.createCategory(name, description, parentCategoryId);
        ra.addFlashAttribute("success", "Category created.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{categoryId}/delete")
    public String deleteCategory(@PathVariable Integer categoryId, RedirectAttributes ra) {
        categoryClient.deleteCategory(categoryId);
        ra.addFlashAttribute("success", "Category deleted.");
        return "redirect:/admin/categories";
    }

    @GetMapping("/tags")
    public String manageTags(Model model) {
        model.addAttribute("tags", categoryClient.getAllTags());
        return "admin/tags";
    }

    @PostMapping("/tags/{tagId}/delete")
    public String deleteTag(@PathVariable Integer tagId, RedirectAttributes ra) {
        categoryClient.deleteTag(tagId);
        ra.addFlashAttribute("success", "Tag deleted.");
        return "redirect:/admin/tags";
    }

    @GetMapping("/comments")
    public String manageComments(Model model) {
        model.addAttribute("pendingComments", commentClient.getAllPendingComments());
        return "admin/comments";
    }

    @PostMapping("/comments/{commentId}/approve")
    public String approveComment(@PathVariable Integer commentId, RedirectAttributes ra) {
        commentClient.approveComment(commentId);
        ra.addFlashAttribute("success", "Comment approved.");
        return "redirect:/admin/comments";
    }

    @PostMapping("/comments/{commentId}/reject")
    public String rejectComment(@PathVariable Integer commentId, RedirectAttributes ra) {
        commentClient.rejectComment(commentId);
        ra.addFlashAttribute("success", "Comment rejected.");
        return "redirect:/admin/comments";
    }

    @PostMapping("/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Integer commentId, RedirectAttributes ra) {
        commentClient.adminDeleteComment(commentId);
        ra.addFlashAttribute("success", "Comment deleted.");
        return "redirect:/admin/comments";
    }

    @GetMapping("/newsletter")
    public String viewSubscribers(Model model) {
        model.addAttribute("subscribers", newsletterClient.getAllSubscribers());
        model.addAttribute("totalCount",  newsletterClient.getSubscriberCount());
        return "admin/newsletter";
    }

    @PostMapping("/newsletter/send")
    public String sendNewsletter(@RequestParam String subject,
                                 @RequestParam String body,
                                 RedirectAttributes ra) {
        newsletterClient.sendCampaign(subject, body);
        ra.addFlashAttribute("success", "Newsletter campaign dispatched!");
        return "redirect:/admin/newsletter";
    }

    @GetMapping("/media")
    public String manageMedia(Model model) {
        model.addAttribute("mediaFiles", mediaClient.getAllMedia());
        return "admin/media";
    }

    @PostMapping("/media/{mediaId}/delete")
    public String deleteMedia(@PathVariable Integer mediaId, RedirectAttributes ra) {
        mediaClient.adminDeleteMedia(mediaId);
        ra.addFlashAttribute("success", "Media file deleted.");
        return "redirect:/admin/media";
    }

    @PostMapping("/notifications/broadcast")
    public String sendPlatformNotification(@RequestParam String title,
                                           @RequestParam String message,
                                           RedirectAttributes ra) {
        notificationClient.broadcastNotification(title, message);
        ra.addFlashAttribute("success", "Broadcast notification sent.");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/analytics")
    public String viewPlatformAnalytics(Model model) {
        model.addAttribute("publishedCount", postClient.getTotalPublishedCount());
        model.addAttribute("userCount",      authClient.getTotalUserCount());
        model.addAttribute("activeSubCount", newsletterClient.getActiveSubscriberCount());
        return "admin/analytics";
    }
}
