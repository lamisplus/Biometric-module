import React, { useState, useEffect } from "react";
import {
  Modal,
  ModalHeader,
  ModalBody,
  Form,
  Table,
  Row,
  Col,
  Card,
  CardBody,
  FormGroup,
  Input,
  Label,
} from "reactstrap";
import Button from "@material-ui/core/Button";
import { makeStyles } from "@material-ui/core/styles";

import { Spinner } from "reactstrap";
import axios from "axios";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { token as token, url as baseUrl } from "./../../../api";

const useStyles = makeStyles((theme) => ({
  card: {
    margin: theme.spacing(20),
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
  },
  form: {
    width: "100%", // Fix IE 11 issue.
    marginTop: theme.spacing(3),
  },
  submit: {
    margin: theme.spacing(3, 0, 2),
  },
  cardBottom: {
    marginBottom: 20,
  },
  Select: {
    height: 45,
    width: 350,
  },
  button: {
    margin: theme.spacing(1),
  },

  root: {
    "& > *": {
      margin: theme.spacing(1),
    },
  },
  input: {
    display: "none",
  },
  error: {
    color: "#f85032",
    fontSize: "11px",
  },
  success: {
    color: "#4BB543 ",
    fontSize: "11px",
  },
}));

const Configuration = (props) => {
  const classes = useStyles();
  const defaultValues = { username: "", password: "", url: "" };
  //console.log(props)
  const [patDetails, setPatDetails] = useState(defaultValues);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState({});
  const [logins, setLogins] = useState([]);

  const handleInputChange = (e) => {
    setPatDetails({ ...patDetails, [e.target.name]: e.target.value });
  };
  /*****  Validation */
  const validate = () => {
    let temp = { ...errors };
    temp.url = patDetails.url ? "" : "PIMS Url is required";
    temp.username = patDetails.username ? "" : "Username is required";
    temp.password = patDetails.password ? "" : "Password is required";

    setErrors({
      ...temp,
    });
    return Object.values(temp).every((x) => x === "");
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validate()) {
      setSaving(true);
      console.log(patDetails);
      axios
        .post(
          `${baseUrl}pims/config?username=${patDetails.username}&password=${patDetails.password}&url=${patDetails.url}`,
          patDetails,
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        )
        .then((response) => {
          setSaving(false);
          // props.NdrSetup();
          toast.success("NDR Setup Successfully saved");
          //props.toggleModal();
          getPIMSConfigurations();
        })
        .catch((error) => {
          setSaving(false);
          if (error.response && error.response.data) {
            let errorMessage =
              error.response.data.apierror &&
              error.response.data.apierror.message !== ""
                ? error.response.data.apierror.message
                : "Something went wrong, please try again";

            toast.error(errorMessage, {
              position: toast.POSITION.BOTTOM_CENTER,
            });
          } else {
            toast.error("Something went wrong. Please try again...", {
              position: toast.POSITION.BOTTOM_CENTER,
            });
          }
        });
    }
  };

  const getPIMSConfigurations = async () => {
    try {
      const response = await axios.get(`${baseUrl}pims/config`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      setLogins(response.data);
    } catch (err) {
      toast.error("An error occurred while fetching config details", {
        position: toast.POSITION.TOP_RIGHT,
      });
    }
  };

  const deleteConfig = async (e, id) => {
    e.preventDefault();
    try {
      //   const response = await axios.delete(`${url}lims/config/${id}`, {
      //     headers: { Authorization: `Bearer ${token}` },
      //   });
      //   console.log(" delete config", response);
      //   loadServerDetails();
      //   props.setConfig({});
      //   toast.success("LIMS Credentials deleted successfully!!", {
      //     position: toast.POSITION.TOP_RIGHT,
      //   });
    } catch (e) {
      toast.error("An error occurred while deleting a config", {
        position: toast.POSITION.TOP_RIGHT,
      });
    }
  };

  useEffect(() => {
    getPIMSConfigurations();
  }, []);

  return (
    <div>
      <Modal
        isOpen={props.showModal}
        toggle={props.toggleModal}
        className={props.className}
        size="lg"
        backdrop={false}
        //backdrop="static"
      >
        <Form>
          <ModalHeader toggle={props.toggleModal}>
            PIMS Configuration Setup
          </ModalHeader>
          <ModalBody>
            <Card>
              <CardBody>
                <Row>
                  <Col md={12}>
                    <FormGroup>
                      <Label>Base URL </Label>
                      <Input
                        type="text"
                        name="url"
                        id="url"
                        value={patDetails.url}
                        onChange={handleInputChange}
                        required
                      />
                      {errors.url !== "" ? (
                        <span className={classes.error}>{errors.url}</span>
                      ) : (
                        ""
                      )}
                    </FormGroup>
                  </Col>
                  <Col md={12}>
                    <FormGroup>
                      <Label>Username </Label>
                      <Input
                        type="text"
                        name="username"
                        id="username"
                        value={patDetails.username}
                        onChange={handleInputChange}
                        required
                      />
                      {errors.username !== "" ? (
                        <span className={classes.error}>{errors.username}</span>
                      ) : (
                        ""
                      )}
                    </FormGroup>
                  </Col>
                  <Col md={12}>
                    <FormGroup>
                      <Label>Password </Label>
                      <Input
                        type="password"
                        name="password"
                        id="password"
                        value={patDetails.password}
                        onChange={handleInputChange}
                        required
                      />
                      {errors.password !== "" ? (
                        <span className={classes.error}>{errors.password}</span>
                      ) : (
                        ""
                      )}
                    </FormGroup>
                  </Col>
                </Row>
                {saving ? <Spinner /> : ""}
                <br />
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  //startIcon={<SettingsBackupRestoreIcon />}
                  onClick={handleSubmit}
                >
                  <span style={{ textTransform: "capitalize " }}>
                    Save PIMS Credentials
                  </span>
                </Button>
                <hr />
                <Row>
                  <Col md={12}>
                    {logins.length === 0 ? (
                      "NO CREDENTIALS IS PROVIDED YET"
                    ) : (
                      <Table bordered size="sm" responsive>
                        <thead
                          style={{
                            backgroundColor: "#014d88",
                            color: "#fff",
                            textAlign: "center",
                          }}
                        >
                          <tr>
                            <th>S/N</th>

                            <th>URL</th>

                            <th>username</th>
                            <th>Password</th>
                            {/*<th>Created Date</th>*/}
                            {/* <th>Actions</th> */}
                          </tr>
                        </thead>
                        <tbody style={{ textAlign: "center" }}>
                          {logins.map((a) => (
                            <tr key={a.id}>
                              <td>{a.id}</td>
                              <td>{a.url}</td>
                              <td>{a.username}</td>
                              <td>******</td>
                              {/* <td>
                              <Button
                                variant="contained"
                                color="error"
                                startIcon={<DeleteIcon />}
                                onClick={(e) => deleteConfig(e, logins.id)}
                              ></Button>
                            </td> */}
                            </tr>
                          ))}
                        </tbody>
                      </Table>
                    )}
                  </Col>
                </Row>
              </CardBody>
            </Card>
          </ModalBody>
        </Form>
      </Modal>
    </div>
  );
};

export default Configuration;
