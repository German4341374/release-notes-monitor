package dev.portfolio.releasemonitor.web;

import dev.portfolio.releasemonitor.error.InvalidProductException;
import dev.portfolio.releasemonitor.product.CheckStatus;
import dev.portfolio.releasemonitor.product.CheckStrategy;
import dev.portfolio.releasemonitor.product.ProductCheckService;
import dev.portfolio.releasemonitor.product.ProductResponse;
import dev.portfolio.releasemonitor.product.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardController {

    private final ProductService productService;
    private final ProductCheckService checkService;

    public DashboardController(ProductService productService, ProductCheckService checkService) {
        this.productService = productService;
        this.checkService = checkService;
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) CheckStatus status, Model model) {
        model.addAttribute("dashboard", productService.dashboard());
        model.addAttribute("products", productService.findAll(status));
        model.addAttribute("statuses", CheckStatus.values());
        model.addAttribute("selectedStatus", status);
        return "dashboard";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        prepareForm(model, new ProductForm(), "Add product", null);
        return "product-form";
    }

    @PostMapping("/products")
    public String create(
            @Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, "Add product", null);
            return "product-form";
        }
        try {
            ProductResponse product = productService.create(form.toRequest());
            redirectAttributes.addFlashAttribute("success", "Product added.");
            return "redirect:/products/" + product.id();
        } catch (InvalidProductException exception) {
            bindingResult.reject("product.invalid", exception.getMessage());
            prepareForm(model, form, "Add product", null);
            return "product-form";
        }
    }

    @GetMapping("/products/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        model.addAttribute("history", productService.history(id));
        return "product-details";
    }

    @GetMapping("/products/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        ProductResponse product = productService.findById(id);
        prepareForm(model, ProductForm.from(product), "Edit product", id);
        return "product-form";
    }

    @PostMapping("/products/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, "Edit product", id);
            return "product-form";
        }
        try {
            productService.update(id, form.toRequest());
            redirectAttributes.addFlashAttribute("success", "Product updated.");
            return "redirect:/products/" + id;
        } catch (InvalidProductException exception) {
            bindingResult.reject("product.invalid", exception.getMessage());
            prepareForm(model, form, "Edit product", id);
            return "product-form";
        }
    }

    @PostMapping("/products/{id}/check")
    public String check(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ProductResponse result = checkService.check(id);
        redirectAttributes.addFlashAttribute(
                "success", "Version check completed: " + result.lastCheckStatus().getDisplayName() + ".");
        return "redirect:/products/" + id;
    }

    @PostMapping("/products/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Product deleted.");
        return "redirect:/";
    }

    private void prepareForm(Model model, ProductForm form, String title, Long productId) {
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", form);
        }
        model.addAttribute("pageTitle", title);
        model.addAttribute("productId", productId);
        model.addAttribute("strategies", CheckStrategy.values());
    }
}
