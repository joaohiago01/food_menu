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
import javax.servlet.http.HttpSession;

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
 * Servlet implementation class ClientServlet
 */
@WebServlet(name = "ClientServlet", urlPatterns = "/ClientServlet")
public class ClientServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@EJB
	ClientBean clientEJB;

	@EJB
	RestaurantBean restaurantEJB;

	@EJB
	MenuBean menuEJB;

	@EJB
	CategoryProductBean categoryProductEJB;
	
	@EJB
	ProductBean productEJB;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ClientServlet() {
		super();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		super.service(request, response);
		if(request.getParameter("_method") != null) {
			doPost(request, response);
		}
	}

	/**
	 * @throws IOException 
	 * @throws ServletException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession httpSession = request.getSession();
		try {

			if (request.getParameter("pageURL") != null && request.getParameter("pageURL").equals("login.jsp")) {//SIGN OUT
				httpSession.invalidate();
				response.sendRedirect("./pages/login.jsp");
			} else {

				String email = request.getParameter("email");
				String password = request.getParameter("password");
				if (email == null && httpSession.getAttribute("email") != null) {//SIGN IN
					email = httpSession.getAttribute("email").toString();
					password = httpSession.getAttribute("password").toString();
					httpSession.removeAttribute("email");
					httpSession.removeAttribute("password");
				}

				if (email != null && password != null && httpSession.getAttribute("clientLogged") == null) {//AUTHENTICATION

//					BASE64Encoder encoder = new BASE64Encoder();
//					String passwordEncrypt = encoder.encode(password.getBytes());
//					Client client = clientEJB.signIn(email, passwordEncrypt);
					Client client = clientEJB.signIn(email, password);
					if (client != null) {
//						BASE64Decoder decoder = new BASE64Decoder();
//						password = new String(decoder.decodeBuffer(client.getPassword()));
//						client.setPassword(password);
						Restaurant restaurant = restaurantEJB.readByUser(client);
						Menu menu = menuEJB.findByRestaurant(restaurant.getId());
						List<Product> products = productEJB.readAllProducts(menu);
						menu.setProducts(products);
						List<Category> categoryProducts = categoryProductEJB.read();

						httpSession.setAttribute("clientLogged", client);
						httpSession.setAttribute("restaurant", restaurant);
						httpSession.setAttribute("menu", menu);
						httpSession.setAttribute("categories", categoryProducts);

						response.sendRedirect("./pages/main.jsp");
					} else {
						response.sendRedirect("./pages/login.jsp");
					}

				} else {//AUTHORIZATION
					int clientID;
					String pageURL = request.getParameter("pageURL");
					if (pageURL == null && httpSession.getAttribute("pageURL") != null) {
						clientID = Integer.parseInt(httpSession.getAttribute("clientID").toString());
						pageURL = httpSession.getAttribute("pageURL").toString();
					} else {
						clientID = Integer.parseInt(request.getParameter("clientID"));
					}

					Client clientLogged = clientEJB.readById(clientID);
					if(clientLogged != null) {

//						BASE64Decoder decoder = new BASE64Decoder();
//						password = new String(decoder.decodeBuffer(clientLogged.getPassword()));
//						clientLogged.setPassword(password);
						Restaurant restaurant = (Restaurant) httpSession.getAttribute("restaurant");
						Menu menu = (Menu) httpSession.getAttribute("menu");
						@SuppressWarnings("unchecked")
						List<Category> categoryProducts = (List<Category>) httpSession.getAttribute("categories");

						httpSession.setAttribute("clientLogged", clientLogged);
						httpSession.setAttribute("restaurant", restaurant);
						httpSession.setAttribute("menu", menu);
						httpSession.setAttribute("categories", categoryProducts);

						response.sendRedirect("./pages/" + pageURL);
					} else {
						response.sendRedirect("./pages/login.jsp");
					}
				}
			}
		} catch (SQLException e) {
			
			throw new ServletException(e);
			
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


		if (request.getParameter("_method") != null) {//PUT METHOD
			doPut(request, response);
		} else {

			Client client = new Client();
			client.setCpf(request.getParameter("cpf"));
			client.setName(request.getParameter("name"));
			client.setEmail(request.getParameter("email"));
			client.setPassword(request.getParameter("password"));
//			BASE64Encoder encoder = new BASE64Encoder();
//			String passwordEncrypt = encoder.encode(request.getParameter("password").getBytes());
//			client.setPassword(passwordEncrypt);

			HttpSession httpSession = request.getSession();
			httpSession.setAttribute("client", client);
			response.sendRedirect("./pages/restaurant_register.jsp");
		}
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {

			HttpSession httpSession = request.getSession();
			Client client = clientEJB.readById(Integer.parseInt(request.getParameter("clientID")));
			client.setCpf(request.getParameter("cpf"));
			client.setName(request.getParameter("name"));
			client.setEmail(request.getParameter("email"));
			client.setPassword(request.getParameter("password"));
//			BASE64Encoder encoder = new BASE64Encoder();
//			String passwordEncrypt = encoder.encode(request.getParameter("password").getBytes());
//			client.setPassword(passwordEncrypt);
			
			client = clientEJB.update(client);

			httpSession.setAttribute("clientID", client.getId());
			httpSession.setAttribute("pageURL", "main.jsp");
			httpSession.setAttribute("clientLogged", client);
			request.getRequestDispatcher("./ClientServlet");
		} catch (SQLException e) {

			throw new ServletException(e);
		}
	}
}
