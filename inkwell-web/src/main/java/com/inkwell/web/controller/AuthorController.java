package com.inkwell.web.controller;

import com.inkwell.web.client.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

/**
 * Author content-management dashboard controller.
 * All endpoints require AUTHOR or ADMIN role.
 * Renders Thymeleaf templates under /templates/author/
 */
@Controller
@RequestMapping("/author")
@PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
@RequiredArgsConstructor
public class AuthorController {

    private final PostClient     postClient;
    private final CommentClient  commentClient;
    private final MediaClient    mediaClient;
    private final CategoryClient categoryClient;

    @GetMapping("/dashboard")
    public String authorDashboard(Principal principal, Model model) {
        model.addAttribute("myPosts",
                postClient.getPostsByAuthorEmail(principal.getName()));
        return "author/dashboard";
    }

    @GetMapping("/posts")
    public String viewMyPosts(Principal principal, Model model) {
        model.addAttribute("posts",
                postClient.getPostsByAuthorEmail(principal.getName()));
        return "author/posts";
    }

    @GetMapping("/posts/new")
    public String createPostForm(Model model) {
        model.addAttribute("categories", categoryClient.getAllCategories());
        model.addAttribute("tags",       categoryClient.getAllTags());
        return "author/post-editor";
    }

    @PostMapping("/posts")
    public String savePost(@RequestParam String title,
                           @RequestParam(required = false) String content,
                           @RequestParam(required = false) String excerpt,
                           @RequestParam(required = false) String featuredImageUrl,
                           Principal principal,
                           RedirectAttributes ra) {
        Map<String, Object> created = postClient.createPost(
                title, content, excerpt, featuredImageUrl, principal.getName());
        Integer postId = created != null ? (Integer) created.get("postId") : null;
        ra.addFlashAttribute("success", "Post saved as draft.");
        return postId != null
                ? "redirect:/author/posts/" + postId + "/edit"
                : "redirect:/author/posts";
    }

    @GetMapping("/posts/{postId}/edit")
    public String editPostForm(@PathVariable Integer postId, Model model) {
        model.addAttribute("post",       postClient.getPostById(postId));
        model.addAttribute("categories", categoryClient.getAllCategories());
        model.addAttribute("tags",       categoryClient.getAllTags());
        return "author/post-editor";
    }

    @PostMapping("/posts/{postId}")
    public String updatePost(@PathVariable Integer postId,
                             @RequestParam(required = false) String title,
                             @RequestParam(required = false) String content,
                             @RequestParam(required = false) String excerpt,
                             @RequestParam(required = false) String featuredImageUrl,
                             Principal principal,
                             RedirectAttributes ra) {
        postClient.updatePost(postId, title, content, excerpt,
                featuredImageUrl, principal.getName());
        ra.addFlashAttribute("success", "Post updated.");
        return "redirect:/author/posts";
    }

    @PostMapping("/posts/{postId}/publish")
    public String publishPost(@PathVariable Integer postId,
                              Principal principal, RedirectAttributes ra) {
        postClient.publishPost(postId, principal.getName());
        ra.addFlashAttribute("success", "Post published!");
        return "redirect:/author/posts";
    }

    @PostMapping("/posts/{postId}/unpublish")
    public String unpublishPost(@PathVariable Integer postId,
                                Principal principal, RedirectAttributes ra) {
        postClient.unpublishPost(postId, principal.getName());
        ra.addFlashAttribute("success", "Post unpublished.");
        return "redirect:/author/posts";
    }

    @PostMapping("/posts/{postId}/delete")
    public String deletePost(@PathVariable Integer postId,
                             Principal principal, RedirectAttributes ra) {
        postClient.deletePost(postId, principal.getName());
        ra.addFlashAttribute("success", "Post deleted.");
        return "redirect:/author/posts";
    }

    @GetMapping("/media")
    public String viewMyMedia(Principal principal, Model model) {
        model.addAttribute("mediaFiles",
                mediaClient.getMediaByUploaderEmail(principal.getName()));
        return "author/media";
    }

    @PostMapping("/media/upload")
    public String uploadMedia(@RequestParam("file") MultipartFile file,
                              Principal principal, RedirectAttributes ra) {
        mediaClient.uploadMedia(file, principal.getName());
        ra.addFlashAttribute("success", "File uploaded successfully.");
        return "redirect:/author/media";
    }

    @PostMapping("/media/{mediaId}/delete")
    public String deleteMedia(@PathVariable Integer mediaId,
                              Principal principal, RedirectAttributes ra) {
        mediaClient.deleteMedia(mediaId, principal.getName());
        ra.addFlashAttribute("success", "File deleted.");
        return "redirect:/author/media";
    }

    @GetMapping("/posts/{postId}/comments")
    public String viewPostComments(@PathVariable Integer postId, Model model) {
        model.addAttribute("postId",   postId);
        model.addAttribute("comments", commentClient.getCommentsForModeration(postId));
        return "author/comments";
    }

    @PostMapping("/comments/{commentId}/approve")
    public String approveComment(@PathVariable Integer commentId,
                                 @RequestParam Integer postId) {
        commentClient.approveComment(commentId);
        return "redirect:/author/posts/" + postId + "/comments";
    }

    @PostMapping("/comments/{commentId}/reject")
    public String rejectComment(@PathVariable Integer commentId,
                                @RequestParam Integer postId) {
        commentClient.rejectComment(commentId);
        return "redirect:/author/posts/" + postId + "/comments";
    }

    @GetMapping("/posts/{postId}/stats")
    public String viewPostStats(@PathVariable Integer postId, Model model) {
        model.addAttribute("post",         postClient.getPostById(postId));
        model.addAttribute("commentCount", commentClient.getCommentCount(postId));
        return "author/post-stats";
    }
}
