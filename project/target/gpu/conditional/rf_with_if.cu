__global__ void RF_with_IF(float *F0, float *F1, float *F2, float *F3, float *F4, float *F5, int *P, const int N)
{	int i = blockIdx.x * blockDim.x + threadIdx.x;
	int Class[3]; 
	Class[0] = 0;
	Class[1] = 0;
	Class[2] = 0;
	if (i < N) {
		if (F[0] <= 48.5) {
			Class[0]++;
		} else {
			if (F[4] <= 1.7) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[3] <= 4.85) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[2] <= 3.35) {
			if (F[0] <= 99.5) {
				if (F[4] <= 0.6) {
					Class[0]++;
				} else {
					Class[1]++;
				}
			} else {
				Class[2]++;
			}
		} else {
			if (F[3] <= 3.75) {
				Class[0]++;
			} else {
				Class[2]++;
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[0] <= 100.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 100.5) {
			if (F[4] <= 0.8) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[1] <= 5.45) {
			if (F[3] <= 2.6) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			if (F[3] <= 4.95) {
				if (F[3] <= 2.55) {
					Class[0]++;
				} else {
					if (F[0] <= 112.0) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				}
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 2.45) {
			Class[0]++;
		} else {
			if (F[3] <= 4.95) {
				if (F[0] <= 114.0) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 100.0) {
			if (F[4] <= 0.7) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[0] <= 100.5) {
			if (F[3] <= 2.6) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[3] <= 2.45) {
			Class[0]++;
		} else {
			if (F[3] <= 4.85) {
				if (F[4] <= 1.7) {
					Class[1]++;
				} else {
					if (F[1] <= 6.05) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				}
			} else {
				if (F[4] <= 1.7) {
					if (F[3] <= 5.35) {
						if (F[0] <= 109.0) {
							Class[1]++;
						} else {
							Class[2]++;
						}
					} else {
						Class[2]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[0] <= 101.0) {
			if (F[1] <= 5.45) {
				if (F[4] <= 0.7) {
					Class[0]++;
				} else {
					Class[1]++;
				}
			} else {
				if (F[0] <= 44.0) {
					Class[0]++;
				} else {
					Class[1]++;
				}
			}
		} else {
			Class[2]++;
		}

		if (F[0] <= 100.5) {
			if (F[0] <= 50.5) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[4] <= 1.55) {
				if (F[3] <= 5.0) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				if (F[0] <= 94.0) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[3] <= 4.85) {
				if (F[4] <= 1.65) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 50.0) {
			Class[0]++;
		} else {
			if (F[4] <= 1.65) {
				if (F[4] <= 1.45) {
					Class[1]++;
				} else {
					if (F[2] <= 2.75) {
						Class[1]++;
					} else {
						if (F[0] <= 95.5) {
							Class[1]++;
						} else {
							Class[2]++;
						}
					}
				}
			} else {
				if (F[0] <= 86.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		}

		if (F[3] <= 2.6) {
			Class[0]++;
		} else {
			if (F[0] <= 102.0) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[3] <= 4.85) {
				if (F[4] <= 1.65) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				if (F[1] <= 6.05) {
					if (F[1] <= 5.9) {
						Class[2]++;
					} else {
						Class[1]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[1] <= 5.45) {
			if (F[0] <= 54.0) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			if (F[3] <= 4.95) {
				if (F[2] <= 3.55) {
					if (F[1] <= 6.0) {
						Class[1]++;
					} else {
						if (F[4] <= 1.7) {
							Class[1]++;
						} else {
							Class[2]++;
						}
					}
				} else {
					Class[0]++;
				}
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 2.45) {
			Class[0]++;
		} else {
			if (F[0] <= 100.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 100.0) {
			if (F[3] <= 2.45) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[0] <= 50.5) {
			Class[0]++;
		} else {
			if (F[0] <= 98.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 4.85) {
			if (F[4] <= 0.8) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[3] <= 4.85) {
			if (F[4] <= 0.8) {
				Class[0]++;
			} else {
				if (F[1] <= 4.95) {
					if (F[4] <= 1.35) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[1]++;
				}
			}
		} else {
			if (F[0] <= 87.0) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 52.0) {
			Class[0]++;
		} else {
			if (F[0] <= 100.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 4.85) {
			if (F[3] <= 2.6) {
				Class[0]++;
			} else {
				if (F[0] <= 103.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		} else {
			if (F[3] <= 4.95) {
				if (F[0] <= 98.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				if (F[1] <= 6.15) {
					if (F[2] <= 2.75) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[0] <= 52.0) {
			Class[0]++;
		} else {
			if (F[3] <= 4.75) {
				Class[1]++;
			} else {
				if (F[0] <= 92.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		}

		if (F[0] <= 50.0) {
			Class[0]++;
		} else {
			if (F[3] <= 4.75) {
				Class[1]++;
			} else {
				if (F[0] <= 93.0) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		}

		if (F[0] <= 52.0) {
			Class[0]++;
		} else {
			if (F[3] <= 4.85) {
				if (F[0] <= 113.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				if (F[2] <= 2.55) {
					if (F[4] <= 1.65) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[0] <= 99.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 2.6) {
			Class[0]++;
		} else {
			if (F[0] <= 99.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 4.85) {
			if (F[3] <= 2.6) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[3] <= 2.45) {
			Class[0]++;
		} else {
			if (F[4] <= 1.7) {
				if (F[0] <= 117.0) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 100.5) {
			if (F[4] <= 0.8) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[1] <= 6.15) {
			if (F[4] <= 0.8) {
				Class[0]++;
			} else {
				if (F[0] <= 101.0) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		} else {
			if (F[3] <= 5.0) {
				if (F[0] <= 98.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 2.45) {
			Class[0]++;
		} else {
			if (F[0] <= 102.0) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 50.0) {
			Class[0]++;
		} else {
			if (F[3] <= 4.95) {
				if (F[0] <= 103.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				if (F[4] <= 1.7) {
					if (F[0] <= 107.0) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[3] <= 5.0) {
				Class[1]++;
			} else {
				if (F[0] <= 93.0) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		}

		if (F[3] <= 2.35) {
			Class[0]++;
		} else {
			if (F[0] <= 101.0) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[3] <= 4.85) {
				if (F[4] <= 1.65) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 52.0) {
			Class[0]++;
		} else {
			if (F[0] <= 100.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 52.5) {
			Class[0]++;
		} else {
			if (F[3] <= 4.75) {
				if (F[0] <= 103.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				if (F[2] <= 2.75) {
					if (F[4] <= 1.7) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[4] <= 1.65) {
				if (F[3] <= 4.95) {
					Class[1]++;
				} else {
					if (F[1] <= 6.05) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				}
			} else {
				if (F[0] <= 86.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		}

		if (F[3] <= 4.85) {
			if (F[0] <= 50.0) {
				Class[0]++;
			} else {
				if (F[3] <= 4.75) {
					Class[1]++;
				} else {
					if (F[1] <= 6.05) {
						Class[1]++;
					} else {
						if (F[1] <= 6.5) {
							Class[2]++;
						} else {
							Class[1]++;
						}
					}
				}
			}
		} else {
			if (F[0] <= 87.0) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 52.0) {
			Class[0]++;
		} else {
			if (F[4] <= 1.55) {
				if (F[0] <= 116.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				if (F[1] <= 6.05) {
					if (F[2] <= 2.6) {
						Class[2]++;
					} else {
						Class[1]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[3] <= 2.6) {
			Class[0]++;
		} else {
			if (F[0] <= 100.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 2.45) {
			Class[0]++;
		} else {
			if (F[3] <= 5.0) {
				if (F[1] <= 4.95) {
					if (F[0] <= 82.5) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					if (F[3] <= 4.75) {
						Class[1]++;
					} else {
						if (F[1] <= 6.05) {
							Class[1]++;
						} else {
							if (F[3] <= 4.85) {
								Class[2]++;
							} else {
								Class[1]++;
							}
						}
					}
				}
			} else {
				Class[2]++;
			}
		}

		if (F[1] <= 5.55) {
			if (F[0] <= 52.0) {
				Class[0]++;
			} else {
				if (F[4] <= 1.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		} else {
			if (F[3] <= 4.85) {
				if (F[3] <= 2.65) {
					Class[0]++;
				} else {
					Class[1]++;
				}
			} else {
				if (F[0] <= 92.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		}

		if (F[4] <= 1.65) {
			if (F[0] <= 51.0) {
				Class[0]++;
			} else {
				if (F[4] <= 1.55) {
					Class[1]++;
				} else {
					if (F[3] <= 5.25) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				}
			}
		} else {
			if (F[0] <= 86.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[3] <= 4.85) {
				if (F[2] <= 2.55) {
					if (F[0] <= 98.5) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[1]++;
				}
			} else {
				if (F[1] <= 6.15) {
					if (F[1] <= 5.9) {
						Class[2]++;
					} else {
						Class[1]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[4] <= 1.65) {
			if (F[1] <= 5.45) {
				if (F[4] <= 0.8) {
					Class[0]++;
				} else {
					Class[1]++;
				}
			} else {
				if (F[3] <= 2.6) {
					Class[0]++;
				} else {
					if (F[0] <= 115.0) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				}
			}
		} else {
			if (F[0] <= 86.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 52.5) {
			Class[0]++;
		} else {
			if (F[0] <= 100.0) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 50.0) {
			Class[0]++;
		} else {
			if (F[0] <= 100.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 101.0) {
			if (F[3] <= 2.45) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[4] <= 0.7) {
			Class[0]++;
		} else {
			if (F[3] <= 4.75) {
				if (F[2] <= 2.55) {
					if (F[0] <= 85.0) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[1]++;
				}
			} else {
				if (F[0] <= 92.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		}

		if (F[1] <= 5.55) {
			if (F[4] <= 0.8) {
				Class[0]++;
			} else {
				if (F[3] <= 4.25) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		} else {
			if (F[0] <= 103.5) {
				if (F[4] <= 0.7) {
					Class[0]++;
				} else {
					Class[1]++;
				}
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 2.6) {
			Class[0]++;
		} else {
			if (F[3] <= 4.85) {
				if (F[0] <= 103.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 100.5) {
			if (F[4] <= 0.8) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[0] <= 47.5) {
			Class[0]++;
		} else {
			if (F[0] <= 102.0) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[1] <= 6.35) {
				if (F[4] <= 1.7) {
					if (F[3] <= 5.0) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					if (F[3] <= 4.85) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				}
			} else {
				if (F[0] <= 91.0) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		}

		if (F[3] <= 2.45) {
			Class[0]++;
		} else {
			if (F[3] <= 5.0) {
				if (F[4] <= 1.65) {
					Class[1]++;
				} else {
					if (F[0] <= 89.0) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				}
			} else {
				if (F[4] <= 1.7) {
					if (F[4] <= 1.55) {
						Class[2]++;
					} else {
						if (F[2] <= 2.85) {
							Class[1]++;
						} else {
							Class[2]++;
						}
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[4] <= 0.7) {
			Class[0]++;
		} else {
			if (F[3] <= 4.85) {
				if (F[0] <= 113.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 52.5) {
			Class[0]++;
		} else {
			if (F[3] <= 4.85) {
				if (F[0] <= 103.0) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				if (F[4] <= 1.7) {
					if (F[0] <= 107.0) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[3] <= 2.35) {
			Class[0]++;
		} else {
			if (F[4] <= 1.7) {
				if (F[0] <= 115.0) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				if (F[0] <= 87.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		}

		if (F[0] <= 100.5) {
			if (F[0] <= 50.5) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[3] <= 4.85) {
			if (F[3] <= 2.45) {
				Class[0]++;
			} else {
				if (F[4] <= 1.7) {
					Class[1]++;
				} else {
					if (F[2] <= 3.0) {
						Class[2]++;
					} else {
						Class[1]++;
					}
				}
			}
		} else {
			Class[2]++;
		}

		if (F[0] <= 100.5) {
			if (F[4] <= 0.7) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[0] <= 52.0) {
			Class[0]++;
		} else {
			if (F[1] <= 6.35) {
				if (F[4] <= 1.65) {
					if (F[0] <= 117.5) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[2]++;
				}
			} else {
				Class[2]++;
			}
		}

		if (F[4] <= 0.7) {
			Class[0]++;
		} else {
			if (F[3] <= 4.75) {
				Class[1]++;
			} else {
				if (F[1] <= 6.0) {
					if (F[2] <= 3.1) {
						Class[2]++;
					} else {
						Class[1]++;
					}
				} else {
					if (F[3] <= 4.95) {
						if (F[4] <= 1.65) {
							Class[1]++;
						} else {
							Class[2]++;
						}
					} else {
						Class[2]++;
					}
				}
			}
		}

		if (F[3] <= 2.45) {
			Class[0]++;
		} else {
			if (F[0] <= 100.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 52.0) {
			Class[0]++;
		} else {
			if (F[0] <= 100.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[1] <= 5.75) {
			if (F[0] <= 49.5) {
				Class[0]++;
			} else {
				if (F[0] <= 103.5) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		} else {
			if (F[3] <= 4.85) {
				if (F[3] <= 4.75) {
					Class[1]++;
				} else {
					if (F[1] <= 6.5) {
						Class[2]++;
					} else {
						Class[1]++;
					}
				}
			} else {
				Class[2]++;
			}
		}

		if (F[4] <= 0.7) {
			Class[0]++;
		} else {
			if (F[0] <= 101.0) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 4.95) {
			if (F[0] <= 52.0) {
				Class[0]++;
			} else {
				if (F[0] <= 103.0) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		} else {
			Class[2]++;
		}

		if (F[0] <= 100.5) {
			if (F[0] <= 50.5) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[0] <= 100.5) {
			if (F[3] <= 2.35) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[2] <= 2.95) {
			if (F[0] <= 103.5) {
				if (F[3] <= 2.2) {
					Class[0]++;
				} else {
					Class[1]++;
				}
			} else {
				Class[2]++;
			}
		} else {
			if (F[3] <= 3.2) {
				Class[0]++;
			} else {
				if (F[3] <= 4.85) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		}

		if (F[3] <= 2.45) {
			Class[0]++;
		} else {
			if (F[1] <= 6.25) {
				if (F[4] <= 1.7) {
					Class[1]++;
				} else {
					if (F[2] <= 3.1) {
						Class[2]++;
					} else {
						Class[1]++;
					}
				}
			} else {
				if (F[4] <= 1.7) {
					if (F[0] <= 103.0) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[0] <= 100.5) {
			if (F[0] <= 50.0) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[0] <= 100.0) {
			if (F[4] <= 0.7) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[4] <= 1.65) {
			if (F[0] <= 50.0) {
				Class[0]++;
			} else {
				if (F[1] <= 7.1) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		} else {
			Class[2]++;
		}

		if (F[0] <= 100.5) {
			if (F[0] <= 50.0) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[1] <= 5.55) {
			if (F[4] <= 0.7) {
				Class[0]++;
			} else {
				if (F[3] <= 4.45) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		} else {
			if (F[3] <= 4.95) {
				if (F[4] <= 1.7) {
					if (F[3] <= 2.5) {
						Class[0]++;
					} else {
						Class[1]++;
					}
				} else {
					Class[2]++;
				}
			} else {
				Class[2]++;
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[4] <= 1.65) {
				if (F[3] <= 5.35) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				if (F[4] <= 1.85) {
					if (F[2] <= 3.15) {
						Class[2]++;
					} else {
						Class[1]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[3] <= 2.45) {
			Class[0]++;
		} else {
			if (F[3] <= 5.0) {
				if (F[1] <= 4.95) {
					Class[2]++;
				} else {
					if (F[0] <= 111.5) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				}
			} else {
				if (F[4] <= 1.7) {
					if (F[3] <= 5.35) {
						if (F[0] <= 109.0) {
							Class[1]++;
						} else {
							Class[2]++;
						}
					} else {
						Class[2]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[3] <= 4.85) {
				Class[1]++;
			} else {
				if (F[4] <= 1.7) {
					if (F[3] <= 5.35) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[3] <= 2.6) {
			Class[0]++;
		} else {
			if (F[4] <= 1.7) {
				if (F[3] <= 5.35) {
					if (F[0] <= 117.0) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[2]++;
				}
			} else {
				if (F[1] <= 6.0) {
					if (F[3] <= 4.95) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[2]++;
				}
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[1] <= 6.05) {
				if (F[4] <= 2.1) {
					if (F[0] <= 103.5) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				} else {
					Class[2]++;
				}
			} else {
				if (F[3] <= 4.8) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[4] <= 1.65) {
				if (F[4] <= 1.35) {
					Class[1]++;
				} else {
					if (F[1] <= 7.0) {
						if (F[0] <= 113.0) {
							Class[1]++;
						} else {
							Class[2]++;
						}
					} else {
						Class[2]++;
					}
				}
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 2.45) {
			Class[0]++;
		} else {
			if (F[4] <= 1.65) {
				if (F[3] <= 5.0) {
					Class[1]++;
				} else {
					if (F[0] <= 107.0) {
						Class[1]++;
					} else {
						Class[2]++;
					}
				}
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 2.5) {
			Class[0]++;
		} else {
			if (F[0] <= 103.0) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[1] <= 5.75) {
			if (F[3] <= 2.35) {
				Class[0]++;
			} else {
				if (F[3] <= 4.45) {
					Class[1]++;
				} else {
					if (F[2] <= 2.65) {
						Class[2]++;
					} else {
						Class[1]++;
					}
				}
			}
		} else {
			if (F[0] <= 100.0) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 2.6) {
			Class[0]++;
		} else {
			if (F[0] <= 100.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[3] <= 4.75) {
			if (F[3] <= 2.45) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			if (F[1] <= 6.05) {
				if (F[4] <= 1.85) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			} else {
				if (F[0] <= 87.0) {
					Class[1]++;
				} else {
					Class[2]++;
				}
			}
		}

		if (F[0] <= 100.0) {
			if (F[0] <= 50.0) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[0] <= 102.0) {
			if (F[3] <= 2.5) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[0] <= 100.0) {
			if (F[3] <= 2.6) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[0] <= 100.5) {
			if (F[3] <= 2.45) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[0] <= 100.5) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}

		if (F[0] <= 100.5) {
			if (F[3] <= 2.45) {
				Class[0]++;
			} else {
				Class[1]++;
			}
		} else {
			Class[2]++;
		}

		if (F[4] <= 0.8) {
			Class[0]++;
		} else {
			if (F[0] <= 100.0) {
				Class[1]++;
			} else {
				Class[2]++;
			}
		}
		int p0 = (Class[0] > Class[1])?0:1;
		int Q0 = (Class[0] > Class[1])?Class[0]:Class[1];
		int p1 = (Class[2] > Q0)?2:p0;
		int Q1 = (Class[2] > Q0)?Class[2]:Q0;
		P[i] = p1;	
	}
}