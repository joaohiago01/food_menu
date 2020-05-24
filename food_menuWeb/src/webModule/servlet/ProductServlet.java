package webModule.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bean.CategoryProductBean;
import bean.ClientBean;
import bean.MenuBean;
import bean.ProductBean;
import bean.RestaurantBean;
import entity.Category;
import entity.Client;
import entity.Menu;
import entity.Product;
import entity.Restaurant;

/**
 * Servlet implementation class ProductServlet
 */
@WebServlet("/ProductServlet")
public class ProductServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@EJB
	ProductBean productEJB;

	@EJB
	CategoryProductBean categoryProductEJB;
	
	@EJB
	ClientBean clientEJB;
	
	@EJB
	RestaurantBean restaurantEJB;

	@EJB
	MenuBean menuEJB;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ProductServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {
			Client clientLogged = clientEJB.readById(Integer.parseInt(request.getParameter("clientID")));
			if (clientLogged != null) {
				Category categoryProduct = categoryProductEJB.readById(Integer.parseInt(request.getParameter("categoryID")));
				Product product = productEJB.readById(Integer.parseInt(request.getParameter("productID")));
				Restaurant restaurant = restaurantEJB.readByUser(clientLogged);
				Menu menu = menuEJB.findByRestaurant(restaurant.getId());
				List<Product> products = productEJB.readAllProducts(menu);
				menu.setProducts(products);
				List<Category> categoryProducts = categoryProductEJB.readByMenu(menu);
				String pageURL = request.getParameter("pageURL");
				
				request.setAttribute("clientLogged", clientLogged);
				request.setAttribute("category", categoryProduct);
				request.setAttribute("categories", categoryProducts);
				request.setAttribute("product", product);
				request.getRequestDispatcher("./pages/" + pageURL).forward(request, response);
				
			} else {
				response.sendRedirect("./pages/login.jsp");
			}
		} catch (SQLException e) {

			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {
			Product product = new Product();
			product.setName(request.getParameter("name"));
			product.setDescription(request.getParameter("description"));
			product.setPrice(Double.parseDouble(request.getParameter("price")));
			Category category = categoryProductEJB.readById(Integer.parseInt(request.getParameter("category")));
			category.getProducts().add(product);
			categoryProductEJB.update(category);
			
			@SuppressWarnings("unused")
			int clientID = Integer.parseInt(request.getParameter("clientID"));
			request.getRequestDispatcher("./ClientServlet?pageURL=products.jsp?&clientID=${clientID}").forward(request, response);
		} catch (SQLException e) {

			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {
			Product product;
			product = productEJB.readById(Integer.parseInt(request.getParameter("productID")));
			product.setName(request.getParameter("name"));
			product.setDescription(request.getParameter("description"));
			product.setPrice(Double.parseDouble(request.getParameter("price")));

			Category category = categoryProductEJB.readById(Integer.parseInt(request.getParameter("category")));
			category.getProducts().add(product);
			categoryProductEJB.update(category);
			
			@SuppressWarnings("unused")
			int clientID = Integer.parseInt(request.getParameter("clientID"));
			request.getRequestDispatcher("./ClientServlet?pageURL=products.jsp?&clientID=${clientID}").forward(request, response);
		} catch (SQLException e) {

			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {
			Product product;
			product = productEJB.readById(Integer.parseInt(request.getParameter("productID")));

			productEJB.delete(product);
			@SuppressWarnings("unused")
			int clientID = Integer.parseInt(request.getParameter("clientID"));
			request.getRequestDispatcher("./ClientServlet?pageURL=products.jsp?&clientID=${clientID}").forward(request, response);
		} catch (SQLException e) {

			throw new ServletException(e);
		}
	}

}
