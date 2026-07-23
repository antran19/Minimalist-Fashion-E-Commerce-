@echo off
echo ========================================================
echo   HSF - Creating Feature Branch & Staging Modified Files
echo ========================================================

git checkout -b feature/stitch-ui-integration

echo.
echo Adding only changed project files...
git add src/main/resources/templates/account.html
git add src/main/resources/static/css/account-stitch.css
git add src/main/resources/templates/cart.html
git add src/main/resources/static/css/cart-stitch.css
git add src/main/resources/templates/products.html
git add src/main/resources/static/css/products-stitch.css
git add src/main/java/com/uminimalist/store/model/ProductView.java
git add src/main/java/com/uminimalist/store/service/LandingPageService.java
git add src/main/java/com/uminimalist/store/controller/HomeController.java

echo.
echo Status of staged files:
git status

echo.
echo Commit files...
git commit -m "feat: integrate Stitch UI (Account, Cart, Products) & fix category sorting"

echo.
echo Pushing to origin...
git push -u origin feature/stitch-ui-integration

echo.
echo ========================================================
echo   Done! Open GitHub/GitLab to create your Pull Request.
echo ========================================================
pause
