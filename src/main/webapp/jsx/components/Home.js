import React, {useState, Fragment } from "react";
import { makeStyles } from "@material-ui/core/styles";
import { Row, Col, Card,  Tab, Tabs, } from "react-bootstrap";
import PrescriptionList from './Prescriptions/PrescriptionList'

const divStyle = {
  borderRadius: "2px",
  fontSize: 14,
};

const Home = () => {
    const [key, setKey] = useState('home');


  return (
    <Fragment>
     
      <Row>
      <div className="row page-titles mx-0" style={{marginTop:"0px", marginBottom:"-10px"}}>
			<ol className="breadcrumb">
				<li className="breadcrumb-item active"><h4>Pharmacy</h4></li>
			</ol>
		</div>
        <Col xl={12}>
          <Card style={divStyle}>
            
            <Card.Body style={{marginTop:"-20px"}}>
              {/* <!-- Nav tabs --> */}
              <div className="custom-tab-1">
                <Tabs
                    id="controlled-tab-example"
                    activeKey={key}
                    onSelect={(k) => setKey(k)}
                    className="mb-3"
                    >
                   
                    <Tab eventKey="home" title="Prescriptions">                   
                      <PrescriptionList />
                    </Tab>
                    
                    </Tabs>


              </div>
            </Card.Body>
          </Card>
        </Col>
        
      </Row>
    </Fragment>
  );
};

export default Home;
