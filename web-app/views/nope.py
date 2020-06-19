# Dash configuration
import dash_core_components as dcc
import dash_html_components as html
from dash.dependencies import Input, Output

from server import app

# Create app layout
layout = html.Div(children=[
    dcc.Location(id='nope_url', refresh=True),
    html.Div(
        className="container",
        children=[
            html.Div(
                children=[html.Img(
                    src='assets/lost.png',
                    style={
                        'max-width': '81%',
                        'display': 'block',
                        'margin-left': 'auto',
                        'margin-right': 'auto'
                    },
                    className='row'
                ),
                html.Div(
                    # children=html.A(html.Button('LogOut'), href='/')
                    children=[
                        html.Button(id='nope_back-button',
                            children='Go to Home',
                            n_clicks=0,
                            style={
                                'margin-top':'20px',
                                'display': 'block',
                                'margin-left': 'auto',
                                'margin-right': 'auto',
                                'background-color':'#a93226',
                                'color':'white'
                            }, className="row")
                    ]
                )
                ]
            )
        ]
    )
])


# Create callbacks
@app.callback(Output('nope_url', 'pathname'),
              [Input('nope_back-button', 'n_clicks')])
def logout_dashboard(n_clicks):
    if n_clicks > 0:
        return '/'
