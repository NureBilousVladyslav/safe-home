from flask import Blueprint, render_template

page_bp = Blueprint('page', __name__)

@page_bp.route('/')
def home():
    return render_template('home_page.html')