package tretornesp.clickerchat3;
public class CategoryAndURL {
    private Category category;
    private String url;

    public CategoryAndURL(Category category, String url) {
        this.category = category;
        this.url = url;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
